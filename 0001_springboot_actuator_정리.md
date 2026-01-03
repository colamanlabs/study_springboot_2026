
# SpringBoot Actuator 정리
```aiignore
2026.01.03
```

```aiignore
SpringBoot acruator 를 통해 springboot 내부 상태를 모니터링 할 수 있다.
웹 API 형태(REST API)로 제공되므로,
SpringBoot web 을 반드시 포함해야 한다.
```

## 1. springboot 최초 pom.xml 에는 스프링부트 액추에이터가 없습니다.

```
맞습니다. ✅
Spring Boot 프로젝트를 처음 생성했을 때의 기본 pom.xml에는 Actuator가 포함되어 있지 않습니다.

아래에서 왜 그런지, 그리고 언제/어떻게 추가하는지 정리해 드리겠습니다.

1️⃣ Spring Boot 기본 pom.xml 구성

Spring Initializr 기준으로 Web 프로젝트를 생성하면 보통 다음 정도만 포함됩니다.

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>


👉 spring-boot-starter-actuator는 기본값이 아닙니다.
```


2️⃣ 왜 Actuator는 기본 포함이 아닐까?
```
🔹 보안 & 운영 관점

Actuator는 다음과 같은 운영·관리 엔드포인트를 노출합니다.

/actuator/health

/actuator/metrics

/actuator/env

/actuator/beans 등

➡️ 잘못 노출되면 보안 리스크가 될 수 있음
➡️ 모든 애플리케이션이 운영 모니터링을 필요로 하지는 않음

🔹 용도 분리 철학

Spring Boot는 다음을 명확히 분리합니다.

구분	역할
starter-web	애플리케이션 기능
starter-data-*	비즈니스 데이터
starter-actuator	운영 / 모니터링

👉 운영 기능은 필요할 때만 명시적으로 추가하는 설계입니다.
```


3️⃣ Actuator를 사용하려면 명시적으로 추가해야 함
```
📌 pom.xml에 직접 추가
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

4️⃣ 추가 후 바로 되는 것 vs 설정이 필요한 것
✅ 기본적으로 활성화되는 것
```
/actuator/health (기본)

/actuator/info (설정 시)

⚠️ 추가 설정이 필요한 것 (application.yml)
management:
endpoints:
web:
exposure:
include: health,info,metrics


👉 아무 설정 안 하면 health와 info만 제한적으로 노출
```

5️⃣ 언제 Actuator를 추가하는 게 좋을까?
```
상황	권장
개인 학습용 / PoC	선택
회사 내부 서비스	✅ 거의 필수
Kubernetes / Cloud	✅ 필수
Prometheus / Grafana 연동	✅ 필수

특히 Spring Boot + Docker + K8s 환경에서는
/actuator/health, /actuator/prometheus가 사실상 표준입니다.
```

6️⃣ 한 줄 요약
```
✔ Spring Boot 최초 pom.xml에는 Actuator가 없다
✔ 운영/모니터링 용도이기 때문에 명시적으로 추가해야 한다
✔ 실서비스에서는 거의 필수 스터터
```




## 2. actuator 의 의미는 무엇입니까?
```
아주 좋은 마무리 질문입니다.
“Actuator”라는 이름 자체가 무엇을 뜻하느냐를 이해하면, 왜 이 기능이 Spring Boot에 존재하는지도 한 번에 정리됩니다.
```

1️⃣ Actuator의 사전적 의미
```
Actuator는 원래 공학 용어입니다.

Actuator = 제어 신호를 받아 실제 동작(행동)을 수행하는 장치

공학·기계 쪽에서의 의미

전기 신호 → 모터 회전

제어 신호 → 밸브 개폐

센서 정보 → 물리적 반응

즉,

“시스템의 상태를 드러내고, 필요하면 조작까지 가능하게 하는 실행 장치”
```


2️⃣ Spring Boot에서 Actuator의 의미
```
Spring Boot에서 Actuator는 이 개념을 그대로 가져옵니다.

애플리케이션의 “현재 상태를 외부에서 관측·점검·관리”할 수 있게 해주는 장치

그래서 Actuator는:

비즈니스 기능용 API가 아니라

운영자·플랫폼(K8s, 모니터링 도구)을 위한 API

입니다.
```


3️⃣ Actuator가 제공하는 것 (개념적으로)
```
🔹 상태 관측 (Observe)

살아있는가? → /actuator/health

메모리는? → /actuator/metrics/jvm.memory.used

요청은? → /actuator/metrics/http.server.requests

🔹 정보 제공 (Inform)

앱 이름/버전 → /actuator/info

빌드 정보 → build-info

환경 정보 → env (제한적)

🔹 제어/관리 (Control)

로깅 레벨 변경

캐시 상태 확인

(과거에는) shutdown 등

👉 **“애플리케이션을 블랙박스로 두지 않겠다”**는 철학의 구현체입니다.

4️⃣ 왜 이름을 Monitoring이나 Admin이라고 안 했을까?

Spring 팀이 Actuator라는 단어를 쓴 이유는 꽤 의도적입니다.

이름	뉘앙스
Monitoring	보기만 함
Admin	사람 중심 관리
Actuator	상태 + 반응 + 제어 가능
```

➡️ 자동화된 시스템(Kubernetes, Cloud, APM) 이 애플리케이션을 “조작”할 수 있어야 하기 때문입니다.
```

예:

K8s → /actuator/health 보고 Pod 재시작

APM → /metrics 수집

운영 자동화 → 상태 기반 판단

5️⃣ 한 문장으로 정의하면

Spring Boot Actuator란,
“애플리케이션의 내부 상태를 외부 세계에 노출하고,
운영 시스템이 이를 기반으로 판단·제어할 수 있게 하는 장치”이다.

또는 ColamanLabs 스타일로 말하면 😊

Actuator는 코드가 아니라 ‘살아있는 애플리케이션’을 다루기 위한 인터페이스다.
```



## 3. 실습
```aiignore
http://127.0.0.1:8080/actuator/health
http://127.0.0.1:8080/actuator/info
```

### 3.1 actuator 조회 실패
```aiignore
실패 1) 
SpringBoot 4 (4.0.1) 로 하고 actuator 추가 했으나, 실패함

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        
SpringBoot 4 에서는 actuator 동작이 기본값에서는 활성화 안된다고 답변 받음

[대책]
우선 SpringBoot 3.5.9 로 변경(3.5.* 대 최신) 함
```

```
실패 2)
SpringBoot 3.5.9 로 변경(3.5.* 대 최신) 하고, pom.xml 에 추가했으나, 실패함 (404)

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        

[원인]
인텔리제이 maven 플러그인과 실제 pom.xml 간 싱크가 안맞았던 것으로 추정

- 1) 우측 메이븐 플러그인 에서 리로드 하고
- 2) 좌측 패널 pom.xml 의 우클릭 maven 메뉴에서 다시 싱크  

동작 성공 함

http://127.0.0.1:8080/actuator/health
{
  "status": "UP"
}
```

### 3.2 actuator 조회시 actuator/health 만 조회가능하고, actuator/info 실패함

```aiignore
[원인]
application.properties 에 설정 필요함


[AS-IS]
spring.application.name=TEST_20260103_0002

[TO-BE]
spring.application.name=TEST_20260103_0002

management.info.env.enabled=true                            # info.app 으로 지정한 내용 출력여부
management.endpoints.web.exposure.include=health,info       # health, info 지정 
info.app.name=TEST_20260103_0002
info.app.version=0.0.1-SNAPSHOT




http://127.0.0.1:8080/actuator/info
{
  "app": {
    "name": "TEST_20260103_0002",
    "version": "0.0.1-SNAPSHOT"
  }
}

http://127.0.0.1:8080/actuator/health
{
  "status": "UP"
}


```
### -- 끝 --
