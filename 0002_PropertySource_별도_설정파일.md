

# @PropertySource 를 통한 별도 프로퍼티 파일 관리

```aiignore
2025.01.03
```

```aiignore
@PropertySource("classpath:dbConfig.properties")
선언으로 별도 프로퍼티 파일을 둘 수 있다.

1) @Value 어노테이션으로 접근할 수 있고,

2) org.springframework.core.env.Environment 선언하고 주입시켜서, getPropterty(..) 로 조회할 수 있다.

ex) 
Environment env; 
...
String user = env.getProperty("user");


[주의사항]
1) application.properties 에 동일 이름의 key 가 있으면, application.properties 의 값으로 우선 처리된다.

2) 하나의 클래스파일에 여러개 선언할 수 있다.

@Configuration
@PropertySource("classpath:dbConfig.properties1")
@PropertySource("classpath:dbConfig.properties2")
@PropertySource("classpath:dbConfig.properties3")
...


3) @PropertySource 는 yml(yaml) 형식은 지원하지 않는다.


[기타]
yml 파일 사용 보다는 .property 파일을 더 권장한다
- 문법오류 가능성
- 공백문자를 실수로 넣지 않거나, 
- 들여쓰기가 잘못되면 오동작
- 전체 이름을 명시하는 .properties 파일을 더 궎장한다. (실전 스프링부트 page 41) 
```

### 스프링부트 설정파일(application.properties, application.yml) 읽는 기본 경로 
```aiignore
1. 클래스패스 루트 

2.클래스패스 /config 패키지 

3. 현재 디렉토리

4. 현재 디렉토리 /config 디렉토리 

5. /config 디렉토리의 바로 하위 디렉토리  
```



```
package com.colamanlabs.springboot2026.test_20260103_0003;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:dbConfig.properties")
public class DBConfiguration
{
    @Value("${user}")
    private String user;

    @Value("${password}")
    private String password;

    public String toString()
    {
        String str = String.format("user:[%s], password:[%s]", user, password);
        return str;
    }
}
```

```
package com.colamanlabs.springboot2026.test_20260103_0003;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
@SpringBootApplication
public class Test202601030003Application
{

    public static void main(String[] args)
    {
        ConfigurableApplicationContext appCtx = SpringApplication.run(Test202601030003Application.class, args);
        DBConfiguration dbConf = appCtx.getBean(DBConfiguration.class);
        log.info(String.format("[Test202601030003Application/main] dbConf:[%s]", dbConf));
    }

}
```

```aiignore
package com.colamanlabs.springboot2026.test_20260103_0003;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class Test202601030003ApplicationTests
{
    @Autowired
    private DBConfiguration dbConfiguration;

    @Test
    void contextLoads()
    {
        String expected = "user:[sa], password:[p@sswOrd]";
        assertEquals(expected, dbConfiguration.toString());
        System.out.println("[DEBUG_LOG] " + dbConfiguration.toString());
    }

}
```

### 기타 참고 옵션
```aiignore
ex)
-- 설정파일을 직접 지정  
java -jar target/SPRING_BOOT_APP.jar --spring.config.name=sbip

-- spring.config.location 프로퍼티 사용 
java -jar target/SPRING_BOOT_APP.jar --spring.config.location=data/sbip.yml
```


-- 끝 -- 
