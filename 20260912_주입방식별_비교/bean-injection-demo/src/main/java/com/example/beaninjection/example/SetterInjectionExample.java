package com.example.beaninjection.example;

import com.example.beaninjection.service.GreetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SetterInjectionExample {
    private GreetingService greetingService;

    // 객체 생성 이후 Spring이 setter를 호출하여 의존성을 주입합니다.
    @Autowired
    public void setGreetingService(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    public String greet(String name) {
        return greetingService.greet(name);
    }
}
