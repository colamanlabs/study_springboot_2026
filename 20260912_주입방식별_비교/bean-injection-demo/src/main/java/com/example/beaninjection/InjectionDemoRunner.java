package com.example.beaninjection;

import com.example.beaninjection.example.ClassTransactionalExample;
import com.example.beaninjection.example.ConstructorInjectionExample;
import com.example.beaninjection.example.FieldInjectionExample;
import com.example.beaninjection.example.MethodTransactionalExample;
import com.example.beaninjection.example.SetterInjectionExample;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InjectionDemoRunner implements CommandLineRunner {
    private final ConstructorInjectionExample constructorExample;
    private final FieldInjectionExample fieldExample;
    private final SetterInjectionExample setterExample;
    private final ClassTransactionalExample classTransactionalExample;
    private final MethodTransactionalExample methodTransactionalExample;

    public InjectionDemoRunner(ConstructorInjectionExample constructorExample,
                               FieldInjectionExample fieldExample,
                               SetterInjectionExample setterExample,
                               ClassTransactionalExample classTransactionalExample,
                               MethodTransactionalExample methodTransactionalExample) {
        this.constructorExample = constructorExample;
        this.fieldExample = fieldExample;
        this.setterExample = setterExample;
        this.classTransactionalExample = classTransactionalExample;
        this.methodTransactionalExample = methodTransactionalExample;
    }

    @Override
    public void run(String... args) {
        System.out.println("[Constructor] " + constructorExample.greet("Spring"));
        System.out.println("[Field]       " + fieldExample.greet("Spring"));
        System.out.println("[Setter]      " + setterExample.greet("Spring"));

        // Spring이 주입한 빈을 통해 @Transactional 선언 메소드를 호출합니다.
        // 현재는 DB와 트랜잭션 매니저가 없으므로 호출 흐름만 실행합니다.
        System.out.println("\n[클래스 @Transactional] 주문 → 결제 → 주문 완료");
        classTransactionalExample.placeOrder();
        classTransactionalExample.cancelOrder();

        System.out.println("\n[메소드 @Transactional] 결제 메소드 직접 호출");
        methodTransactionalExample.processPayment();

        System.out.println("\n[@Transactional 없는 메소드] 결제 안내");
        methodTransactionalExample.printPaymentGuide();
    }
}
