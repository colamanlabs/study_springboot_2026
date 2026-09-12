package com.example.beaninjection.example;

import com.example.beaninjection.service.GreetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FieldInjectionExample {
    // 객체 생성 이후 Spring이 필드에 의존성을 주입합니다.
    @Autowired
    private GreetingService greetingService;

    public String greet(String name) {
        return greetingService.greet(name);
    }
}
