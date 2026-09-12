package com.example.beaninjection;

import com.example.beaninjection.example.ConstructorInjectionExample;
import com.example.beaninjection.example.FieldInjectionExample;
import com.example.beaninjection.example.SetterInjectionExample;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InjectionDemoRunner implements CommandLineRunner {
    private final ConstructorInjectionExample constructorExample;
    private final FieldInjectionExample fieldExample;
    private final SetterInjectionExample setterExample;

    public InjectionDemoRunner(ConstructorInjectionExample constructorExample,
                               FieldInjectionExample fieldExample,
                               SetterInjectionExample setterExample) {
        this.constructorExample = constructorExample;
        this.fieldExample = fieldExample;
        this.setterExample = setterExample;
    }

    @Override
    public void run(String... args) {
        System.out.println("[Constructor] " + constructorExample.greet("Spring"));
        System.out.println("[Field]       " + fieldExample.greet("Spring"));
        System.out.println("[Setter]      " + setterExample.greet("Spring"));
    }
}
