# Spring Boot Bean 주입 방식 예제

Java 17 / Spring Boot 4.1.1 / Maven 기반 콘솔 프로젝트입니다.
DB나 웹 서버 설정 없이 실행하여 세 가지 의존성 주입 방식을 비교합니다.

## 예제 클래스

| 방식 | 클래스 (src/main/java/com/example/beaninjection/example) | 특징 |
| --- | --- | --- |
| 생성자 주입 | ConstructorInjectionExample.java | 생성자 인자로 주입, final 가능, 단일 생성자는 @Autowired 생략 가능 |
| 필드 주입 | FieldInjectionExample.java | 필드의 @Autowired로 주입, Spring 없이 직접 대역을 넣기 어려움 |
| setter 주입 | SetterInjectionExample.java | setter의 @Autowired로 주입, 생성 후 의존성 변경 가능 |

GreetingService는 @Service로 등록되며 세 예제는 @Component로 등록됩니다.
기본 singleton 범위이므로 같은 Spring 컨테이너 안에서 동일한 GreetingService 인스턴스를 공유합니다.
생성자 주입은 필수 의존성을 생성 시점에 명시할 수 있어 일반적으로 권장됩니다.
setter의 @Autowired도 기본적으로 필수 주입이며, setter 방식 자체가 선택적 주입을 의미하지 않습니다.
필드와 setter의 자동 주입은 Spring이 관리하는 객체에 적용됩니다. 직접 new로 생성하면 자동 주입되지 않습니다.

## 실행

프로젝트 디렉터리에서:

```powershell
mvn spring-boot:run
```

출력 (Spring 시작 로그 이후):

```text
[Constructor] Hello, Spring!
[Field]       Hello, Spring!
[Setter]      Hello, Spring!
```

출력 후 종료되는 콘솔 애플리케이션입니다.

## 테스트 및 패키징

```powershell
mvn test
mvn clean package
java -jar target/bean-injection-demo-0.0.1-SNAPSHOT.jar
```

- InjectionExamplesTest: 실제 Spring 컨테이너에서 각 방식의 실행 결과와 동일 Bean 공유를 확인합니다.
- ManualInjectionTest: Spring 없이 생성자와 setter로 테스트 대역을 전달하는 방법을 보여줍니다.
- 총 6개 테스트이며, 최초 빌드 시 Maven 의존성 다운로드를 위한 인터넷 연결이 필요합니다.

## IntelliJ IDEA

1. File > Open에서 이 폴더의 pom.xml을 선택하여 프로젝트로 엽니다.
2. Project SDK와 Maven Runner JRE를 Java 17 이상으로 설정합니다.
3. Maven 프로젝트 동기화 후 BeanInjectionApplication.main()을 실행합니다.
4. src/test/java의 테스트 클래스 또는 Maven의 test를 실행합니다.

## 참고

- [Spring Boot 시스템 요구 사항](https://docs.spring.io/spring-boot/system-requirements.html)
