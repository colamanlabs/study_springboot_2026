package com.example.beaninjection;

import com.example.beaninjection.example.ConstructorInjectionExample;
import com.example.beaninjection.example.FieldInjectionExample;
import com.example.beaninjection.example.SetterInjectionExample;
import com.example.beaninjection.service.GreetingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class InjectionExamplesTest {
    @Autowired
    private ConstructorInjectionExample constructorExample;
    @Autowired
    private FieldInjectionExample fieldExample;
    @Autowired
    private SetterInjectionExample setterExample;
    @Autowired
    private GreetingService greetingService;

    @Test
    @DisplayName("생성자 주입 Bean이 정상 동작한다")
    void constructorInjection() {
        assertThat(constructorExample.greet("Spring")).isEqualTo("Hello, Spring!");
    }

    @Test
    @DisplayName("필드 주입 Bean이 정상 동작한다")
    void fieldInjection() {
        assertThat(fieldExample.greet("Spring")).isEqualTo("Hello, Spring!");
    }

    @Test
    @DisplayName("setter 주입 Bean이 정상 동작한다")
    void setterInjection() {
        assertThat(setterExample.greet("Spring")).isEqualTo("Hello, Spring!");
    }

    @Test
    @DisplayName("세 방식 모두 동일한 singleton Bean을 주입받는다")
    void sharesSingletonBean() {
        for (Object example : new Object[]{constructorExample, fieldExample, setterExample}) {
            assertThat(ReflectionTestUtils.getField(example, "greetingService"))
                    .isSameAs(greetingService);
        }
    }
}
