package com.example.beaninjection;

import com.example.beaninjection.example.ConstructorInjectionExample;
import com.example.beaninjection.example.SetterInjectionExample;
import com.example.beaninjection.service.GreetingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ManualInjectionTest {
    private final GreetingService stub = new GreetingService() {
        @Override
        public String greet(String name) {
            return "Test greeting";
        }
    };

    @Test
    @DisplayName("생성자 주입은 Spring 없이 테스트 대역을 전달할 수 있다")
    void constructorAcceptsTestDouble() {
        var example = new ConstructorInjectionExample(stub);
        assertThat(example.greet("Spring")).isEqualTo("Test greeting");
    }

    @Test
    @DisplayName("setter 주입은 Spring 없이 setter로 테스트 대역을 전달할 수 있다")
    void setterAcceptsTestDouble() {
        var example = new SetterInjectionExample();
        example.setGreetingService(stub);
        assertThat(example.greet("Spring")).isEqualTo("Test greeting");
    }
}
