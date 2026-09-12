# Spring Boot 트랜잭션 사용 정리

## 1. 현재 예제의 범위

이 프로젝트는 DB 연결 없이 `@Transactional`의 선언 방식과 호출 흐름을 살펴보는 예제입니다. 트랜잭션 매니저와 트랜잭션 활성화 설정이 없으므로 현재 실행에서는 트랜잭션 프록시에 의한 시작·커밋·롤백이 수행되지 않습니다.

아래 설명은 Spring의 기본 프록시 기반 트랜잭션 관리가 활성화되어 있고, 적절한 트랜잭션 매니저가 구성된 경우를 전제로 합니다. DB 작업은 같은 스레드에서 해당 트랜잭션에 참여하는 자원을 사용한다고 가정합니다.

관련 파일:

- [InjectionDemoRunner.java](src/main/java/com/example/beaninjection/InjectionDemoRunner.java): 주입받은 빈의 메소드를 호출하는 진입점
- [ClassTransactionalExample.java](src/main/java/com/example/beaninjection/example/ClassTransactionalExample.java): 클래스에 선언한 예제
- [MethodTransactionalExample.java](src/main/java/com/example/beaninjection/example/MethodTransactionalExample.java): 메소드에 선언한 예제

## 2. 클래스 선언과 메소드 선언

```java
@Service
@Transactional
public class OrderService {
    public void placeOrder() { }
    public void cancelOrder() { }
}
```

클래스 선언은 적용 가능한 메소드들의 공통 트랜잭션 설정입니다. 위 예제에서는 두 public 메소드가 대상입니다. 메소드에 별도 설정을 선언하면 해당 메소드의 설정이 우선합니다.

```java
@Service
public class PaymentService {
    @Transactional
    public void pay() { }

    public void guide() { }
}
```

메소드에만 선언해도 빈 자체가 프록시로 주입됩니다. `pay()`에는 트랜잭션 처리가 적용되고, `guide()` 호출 자체는 트랜잭션을 시작하지 않습니다. 다만 호출자가 이미 트랜잭션을 시작했다면 `guide()`도 그 실행 범위 안에서 동작합니다.

## 3. 프록시는 언제 주입되는가?

일반적인 빈 생성·주입 흐름은 다음과 같습니다.

1. Spring이 원본 객체를 생성하고 의존성 주입과 초기화를 수행합니다.
2. 후처리 과정에서 트랜잭션 적용 대상인 빈을 프록시로 감쌉니다.
3. 해당 빈을 사용하는 다른 빈에 프록시 객체를 주입합니다.

따라서 호출할 때 원본이 프록시로 바뀌는 것이 아니라, 주입받은 참조가 이미 프록시입니다.

```text
InjectionDemoRunner
    → 주입받은 프록시.placeOrder()
    → 트랜잭션 인터셉터
    → 원본.placeOrder()
```

클래스 기반 프록시는 대상 클래스를 상속합니다. 인터페이스 기반 프록시는 인터페이스를 구현합니다. 실제 호출은 단순히 `super.placeOrder()`를 실행한다고 이해하기보다는, 인터셉터를 거쳐 원본 객체에 위임한다고 이해하면 됩니다.

프록시의 각 메소드 본문에 롤백 코드를 직접 생성하는 형태라기보다는, 호출 시 인터셉터가 클래스와 메소드의 트랜잭션 설정을 바탕으로 처리합니다.

## 4. 정상 종료와 예외 발생

새 트랜잭션을 시작한 경우의 기본 흐름은 다음과 같습니다.

```text
프록시 진입 → 트랜잭션 시작 → 원본 메소드 실행
    ├─ 정상 종료 → 커밋
    └─ RuntimeException 또는 Error 전파 → 롤백 → 호출자에게 예외 재전달
```

롤백한 뒤 예외를 삼키고 정상 반환하는 것은 아닙니다. 체크 예외는 기본적으로 롤백 대상이 아니며, 필요한 경우 다음과 같이 지정합니다.

```java
@Transactional(rollbackFor = Exception.class)
public void placeOrder() throws Exception {
    // 체크 예외도 롤백 대상에 포함
}
```

이미 존재하는 트랜잭션에 참여했다면, 참여 메소드가 끝났다고 독립적으로 커밋하지 않습니다. 롤백 대상 예외가 발생하면 공유 트랜잭션을 롤백 전용 상태로 표시할 수 있으며, 최종 완료는 바깥 트랜잭션 경계에서 결정됩니다.

## 5. 내부 호출로 트랜잭션을 시작할 수 없는 경우

```java
@Service
public class PaymentService {
    public void order() {
        this.pay();
    }

    @Transactional
    public void pay() {
        // DB 작업
    }
}
```

```text
외부 → 프록시.order() → 원본.order() → 원본.pay()
```

`order()`에 트랜잭션 설정이 없고 기존 트랜잭션도 없다면, `pay()`는 트랜잭션 없이 실행됩니다. 원본 객체가 `this.pay()`로 직접 호출하므로 프록시와 트랜잭션 인터셉터를 거치지 않기 때문입니다. `this.`를 생략해도 같습니다.

해결 방법은 작업 전체의 경계인 `order()`에 `@Transactional`을 선언하거나, `pay()`를 별도 Spring 빈으로 분리하고 주입받은 빈을 통해 호출하는 것입니다.

## 6. 바깥 메소드에서 트랜잭션을 시작하고 세부 메소드를 호출하는 경우

```java
@Service
public class OrderService {
    @Transactional
    public void placeOrder() {
        validateOrder();
        saveOrder();
        processPayment();
    }

    private void validateOrder() {
        // 주문 검증
    }

    private void saveOrder() {
        // 같은 트랜잭션에 참여하는 DB 저장 작업
    }

    private void processPayment() {
        throw new RuntimeException("결제 실패");
    }
}
```

외부에서 프록시를 통해 `placeOrder()`를 호출하면 다음과 같이 동작합니다.

```text
외부 → 프록시: 트랜잭션 시작
     → 원본.placeOrder()
         → validateOrder()
         → saveOrder()
         → processPayment(): RuntimeException 발생
     → 예외가 placeOrder() 밖으로 전파
     → 프록시: 트랜잭션 롤백 후 예외 재전달
```

세부 메소드에 `@Transactional`을 붙일 필요는 없습니다. 이미 시작된 트랜잭션 범위에서 동작하기 때문에 private 메소드로 분리해도 됩니다.

- 모든 작업이 정상 종료하면 바깥 트랜잭션 경계에서 커밋합니다.
- 세부 메소드의 `RuntimeException`이 `placeOrder()` 밖으로 전파되면 기본적으로 전체 트랜잭션을 롤백합니다.
- 내부 호출 문제는 내부 메소드의 어노테이션으로 새로운 설정을 적용하지 못한다는 의미입니다. 이미 시작된 트랜잭션이 사라지는 것은 아닙니다.
- 내부 메소드에 `REQUIRES_NEW` 등을 선언해도 직접 내부 호출하면 그 설정은 적용되지 않습니다.

## 7. 예외를 잡고 정상 반환하는 경우

```java
@Transactional
public void placeOrder() {
    saveOrder();

    try {
        processPayment();
    } catch (RuntimeException e) {
        log.error("결제 실패", e);
        // 예외를 다시 던지지 않고 정상 반환
    }
}
```

프록시까지 예외가 전달되지 않으면 정상 종료로 판단하여 커밋될 수 있습니다. 전체 작업을 실패시켜야 한다면 일반적으로 예외를 다시 던져 바깥 트랜잭션 경계까지 전달합니다.

다만 별도 빈의 트랜잭션 인터셉터나 DB 접근 계층이 이미 트랜잭션을 롤백 전용 상태로 표시했다면, 예외를 잡아도 커밋할 수 없습니다. 바깥에서 커밋을 시도할 때 `UnexpectedRollbackException`이 발생할 수 있습니다.

## 8. 추가 주의사항

| 항목 | 주의할 점 |
| --- | --- |
| 직접 객체 생성 | `new`로 만든 객체에는 Spring의 트랜잭션 프록시가 적용되지 않습니다. |
| 기본 전파 속성 | `REQUIRED`는 기존 트랜잭션이 있으면 참여하고, 없으면 새로 시작합니다. |
| 접근 제한자 | 일반적으로 public 메소드에 선언하면 명확합니다. private 메소드는 프록시로 가로챌 수 없습니다. |
| final | 클래스 기반 프록시는 final 클래스를 상속하거나 final 메소드를 재정의할 수 없습니다. |
| 새 스레드 | 일반적인 스레드 기반 트랜잭션은 `@Async`나 새 스레드에 자동 전달되지 않습니다. |
| 롤백 범위 | 해당 트랜잭션에 참여한 DB 작업이 대상입니다. 이메일, 외부 API 요청, 파일 변경, 메모리 필드값까지 자동 복구되지는 않습니다. |
| 실행 시간 | 느린 외부 호출과 긴 대기는 DB 연결 및 잠금 점유 시간을 늘릴 수 있으므로 트랜잭션 범위를 신중하게 잡습니다. |

## 9. 현재 프로젝트의 호출 부분

`InjectionDemoRunner`는 생성자로 두 예제 빈을 주입받고, 애플리케이션 시작 시 다음 메소드를 호출합니다.

```java
// 클래스에 @Transactional 선언
classTransactionalExample.placeOrder(); // 내부에서 다른 빈의 processPayment() 호출
classTransactionalExample.cancelOrder();

// 메소드에 @Transactional 선언
methodTransactionalExample.processPayment();

// 트랜잭션 설정이 없는 메소드
methodTransactionalExample.printPaymentGuide();
```

실제 트랜잭션 구성을 추가하면 `placeOrder()`에서 호출하는 다른 빈의 `processPayment()`는 기본 `REQUIRED` 설정에 따라 주문 트랜잭션에 참여합니다. Runner에서 `processPayment()`를 별도로 호출하면 기존 트랜잭션이 없으므로 새 트랜잭션을 시작합니다.
