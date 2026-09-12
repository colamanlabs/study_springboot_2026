# Spring 의존성 주입 대화 정리

이 문서는 `bean-injection-demo` 프로젝트를 살펴보며 나눈 대화를 주제별로 정리한 학습 노트입니다.

## 1. 프로젝트 개요와 분석 결과

- Java 17, Spring Boot 4.1.1, Maven 기반 콘솔 애플리케이션입니다.
- 생성자 주입, 필드 주입, Setter 주입을 비교합니다.
- `GreetingService`는 `@Service`, 각 주입 예제와 실행기는 `@Component`로 등록됩니다.
- `InjectionDemoRunner`가 세 예제의 `greet()`를 호출하여 인사말을 출력합니다.
- 세 예제는 같은 Spring 컨테이너에서 동일한 `GreetingService` 싱글턴 Bean을 공유합니다.
- 대화 중 `mvn -o test`를 실행했으며 테스트 6개가 모두 통과했습니다. 통합 테스트 4개와 수동 주입 테스트 2개입니다.

분석 당시 주요 기능 오류는 발견하지 못했습니다. Maven Wrapper가 없고, `greet(null)`은 `"Hello, null!"`을 반환하며, 싱글턴 확인 테스트는 리플렉션으로 필드명을 참조합니다. 해당 폴더는 Git 저장소가 아니었습니다.

## 2. 필드 주입과 Setter 주입

### 필드 주입

필드에 `@Autowired`를 지정하면 Spring이 해당 필드에 의존성을 설정합니다.

```java
@Autowired
private GreetingService greetingService;
```

이 대화에서 사용한 “멤버 주입”은 멤버 필드를 대상으로 하는 필드 주입이라는 의미입니다.

### Setter 주입

Setter 메서드에 `@Autowired`를 지정하면 Spring이 의존성을 인자로 전달하여 메서드를 호출합니다.

```java
private GreetingService greetingService;

@Autowired
public void setGreetingService(GreetingService greetingService) {
    this.greetingService = greetingService;
}
```

`public` Setter는 일반 외부 코드에서도 호출할 수 있으므로 나중에 의존성을 교체할 수 있습니다. Setter에는 검증이나 부가 로직도 넣을 수 있습니다.

Setter의 `@Autowired`도 기본적으로 필수 주입입니다. Setter 방식 자체가 선택적 의존성을 의미하지는 않습니다.

### private Setter에도 주입할 수 있는가?

가능합니다.

```java
@Autowired
private void setGreetingService(GreetingService greetingService) {
    this.greetingService = greetingService;
}
```

Spring은 리플렉션으로 `private` 필드에 접근하거나 `private` 메서드를 호출할 수 있습니다. `@Autowired`가 접근 제어자를 바꾸는 것은 아니므로 일반 외부 Java 코드에서는 여전히 직접 접근할 수 없습니다.

따라서 `private` Setter와 `private` 필드 주입은 외부 접근 측면에서 비슷합니다. 다만 필드 주입은 값을 직접 설정하고, Setter 주입은 메서드의 코드를 실행합니다.

`private`은 외부 접근을 제한할 뿐입니다. 클래스 내부에서 필드를 변경하는 것까지 막지는 않습니다.

## 3. 리플렉션의 setAccessible(true)

`setAccessible(true)`는 해당 리플렉션 객체를 통한 접근에서 Java 언어의 접근 검사를 억제하도록 설정하는 것입니다.

```java
Field field = Example.class.getDeclaredField("service");

field.setAccessible(true);
field.set(example, service);

// 접근 검사를 다시 활성화하려면 명시적으로 호출합니다.
field.setAccessible(false);
```

핵심은 다음과 같습니다.

- 대상 클래스의 `private`이나 `final` 선언을 제거하는 것이 아닙니다.
- 상태는 대상 Bean이 아니라 접근에 사용하는 `Field` 또는 `Method` 리플렉션 객체에 적용됩니다.
- 값을 할당하거나 메서드를 호출했다고 자동으로 `false`로 돌아가지는 않습니다.
- 현재 메서드 실행이 끝나도, 해당 리플렉션 객체를 다른 곳에서 참조하면 설정 상태는 유지됩니다.
- 더 이상 참조되지 않으면 GC 대상이 됩니다. 이것은 `false`로 복구되는 것과 다릅니다.
- `setAccessible(false)`는 이미 할당한 값을 되돌리지 않습니다.
- Spring의 필드 주입은 값을 설정한 뒤 매번 `setAccessible(false)`를 호출하는 방식이 아닙니다.
- Java 모듈 경계 등의 제한까지 무조건 무시할 수 있는 것은 아닙니다.

## 4. final 필드와 주입

### 초기화하지 않은 final 필드

```java
private final GreetingService greetingService;
```

이 선언 자체가 항상 오류인 것은 아닙니다. 선언부에서 초기화하지 않았다면 생성자에서 반드시 할당해야 합니다. 그러한 생성자도 없으면 컴파일 오류가 발생합니다.

일반 참조 필드의 기본값은 `null`이지만, 그 기본값만으로 `final` 필드의 명시적 초기화 의무가 충족되지는 않습니다. `@Autowired`도 컴파일러의 초기화 규칙을 대신 충족하지 않습니다.

### null로 초기화한 final 필드

```java
@Autowired
private final GreetingService greetingService = null;
```

명시적으로 초기화했으므로 컴파일은 가능합니다. Java 17의 일반 클래스 인스턴스 필드는 조건을 충족하면 Spring의 리플렉션 기반 처리로 값이 주입될 수 있습니다.

이것은 `@Autowired`가 `final` 변경을 특별히 허용하거나, Spring이 `final`을 잠시 해제했다가 복구하기 때문이 아닙니다. 필드 선언은 그대로이고 Java 리플렉션이 허용하는 경로로 값을 설정하는 것입니다.

다만 이러한 사후 변경에 의존하는 설계는 피하는 것이 좋습니다. `final`을 사용하려면 생성자에서 정상적으로 초기화하는 방식이 적절합니다.

### Java 버전과 필드 종류에 따른 차이

| 조건 | 리플렉션을 통한 final 필드 수정 |
| --- | --- |
| Java 17의 일반 클래스 인스턴스 필드 | 접근 검사 억제 등 조건을 충족하면 가능 |
| Java 17의 static final 필드 또는 record의 final 필드 | `setAccessible(true)`로도 수정 불가 |
| Java 26 | 별도 허용 없는 final 필드 변경은 기본적으로 경고하며, JVM 옵션으로 차단 가능 |

Java 26 문서는 향후 버전에서 기본 차단으로 전환할 계획을 설명합니다. 수정 가능 여부는 Java 버전, 필드 종류, 모듈 접근 조건, JVM 실행 설정에 따라 달라집니다.

일반 Java 코드에서 초기화가 끝난 `final` 필드를 재할당할 수 없다는 규칙은 그대로입니다.

## 5. 생성자 주입과 @Autowired 생략

Spring이 관리하는 클래스에 생성자가 하나뿐이면 `@Autowired`를 생략해도 해당 생성자로 의존성을 주입합니다.

```java
@Component
public class ConstructorInjectionExample {
    private final GreetingService greetingService;

    public ConstructorInjectionExample(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    public String greet(String name) {
        return greetingService.greet(name);
    }
}
```

Spring이 `GreetingService` Bean을 생성자 인자로 전달하고, 생성자가 필드를 초기화합니다. `final` 필드를 생성 이후 리플렉션으로 변경할 필요가 없습니다.

## 6. 생성자 주입이 권장되는 이유

### 생성 시점에 필수 의존성을 전달할 수 있음

생성자에서 의존성을 받아 필드에 할당하므로 생성자가 완료되면 해당 의존성을 사용할 준비가 됩니다. 필수 생성자 의존성을 Spring이 찾지 못하면 Bean 생성에 실패합니다.

직접 `new`로 호출할 때는 호출자가 `null`을 전달할 수도 있습니다. 생성자 문법 자체가 null을 금지하는 것은 아니므로 필요하면 `Objects.requireNonNull()` 등으로 검증합니다.

### final로 참조 재할당을 막을 수 있음

```java
private final GreetingService greetingService;
```

생성자에서 초기화한 뒤 일반 코드에서 다른 객체로 재할당하는 것을 막습니다. 단, 고정되는 것은 참조이며 주입된 객체 내부 상태까지 불변이 되는 것은 아닙니다.

### 필요한 의존성이 생성자에 드러남

```java
public ConstructorInjectionExample(GreetingService greetingService)
```

생성자의 매개변수 타입을 보면 이 객체를 만들려면 `GreetingService`가 필요하다는 사실을 알 수 있습니다.

```java
new ConstructorInjectionExample();        // 인자가 없어 컴파일 오류
new ConstructorInjectionExample(service); // 필요한 의존성을 전달
```

### Spring 없이 테스트 대역을 전달하기 쉬움

실제 서비스를 대신하는 테스트용 객체를 만들고 생성자에 직접 전달할 수 있습니다.

```java
GreetingService stub = new GreetingService() {
    @Override
    public String greet(String name) {
        return "테스트 인사";
    }
};

var example = new ConstructorInjectionExample(stub);

assertThat(example.greet("Spring")).isEqualTo("테스트 인사");
```

Spring 컨테이너나 `@SpringBootTest` 없이 일반 Java 객체 생성만으로 테스트합니다. 프로젝트의 `ManualInjectionTest`가 이 방식을 보여줍니다.

의존성을 전달하는 역할을 실행 시에는 Spring이, 단위 테스트에서는 테스트 코드가 수행합니다.

공개된 Setter가 있으면 Setter 주입도 직접 테스트 대역을 전달할 수 있습니다. 반면 `private` 필드 주입은 일반 외부 코드로 값을 설정할 수 없어 리플렉션 등의 별도 수단이 필요합니다.

## 7. 생성자 주입과 싱글턴은 별개의 개념

생성자 주입이 권장되는 이유를 “Bean이 한 번만 생성되기 때문”으로 설명하는 것은 정확하지 않습니다.

- 기본 싱글턴 범위는 Spring 컨테이너의 Bean 정의마다 하나의 인스턴스를 공유한다는 의미입니다.
- 클래스 전체나 JVM 전체에서 객체가 단 한 번만 생성된다는 보장은 아닙니다.
- 필드 주입과 Setter 주입에도 같은 싱글턴 범위가 적용됩니다.
- `prototype` 등 다른 범위를 사용하면 인스턴스가 여러 번 생성될 수 있습니다.
- 생성자 주입의 장점은 싱글턴 여부와 관계없이 적용됩니다.

또한 `@Component`는 클래스에 붙이고, `@Bean`은 객체를 생성해 반환하는 메서드에 붙입니다. 생성자 주입이라는 설계 방식 자체는 Spring 어노테이션 없이도 사용할 수 있습니다.

## 참고 자료

- [Spring: @Autowired API](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/beans/factory/annotation/Autowired.html)
- [Spring: 의존성 주입과 생성자 주입 권장 이유](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html)
- [Spring: Bean 범위](https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html)
- [Spring: AutowiredAnnotationBeanPostProcessor 구현](https://github.com/spring-projects/spring-framework/blob/main/spring-beans/src/main/java/org/springframework/beans/factory/annotation/AutowiredAnnotationBeanPostProcessor.java)
- [Java 17: Field와 final 필드 수정 조건](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/reflect/Field.html#set(java.lang.Object,java.lang.Object))
- [Java 26: final 필드 변경 제한 준비](https://docs.oracle.com/en/java/javase/26/migrate/preparing-final-field-mutation-restrictions.html)
