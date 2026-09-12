package com.example.beaninjection.example;

import com.example.beaninjection.service.GreetingService;
import org.springframework.stereotype.Component;

@Component
public class ConstructorInjectionExample {
    private final GreetingService greetingService;

    // 생성자가 하나이면 @Autowired를 생략할 수 있습니다.
    public ConstructorInjectionExample(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    public String greet(String name) {
        return greetingService.greet(name);
    }
}
