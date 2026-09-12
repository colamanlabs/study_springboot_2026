package com.example.beaninjection.service;

import org.springframework.stereotype.Service;

// 세 예제에 공통으로 주입되는 기본 singleton Bean입니다.
@Service
public class GreetingService {
    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}
