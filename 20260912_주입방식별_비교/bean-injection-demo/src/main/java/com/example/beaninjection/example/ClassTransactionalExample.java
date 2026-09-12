package com.example.beaninjection.example;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 클래스에 선언하면 public 메소드에 공통 트랜잭션 설정이 적용됩니다.
 * 선언과 호출 흐름만 보여주는 예제이며, DB와 트랜잭션 매니저는 구성하지 않습니다.
 * 실제 트랜잭션 처리를 위해서는 트랜잭션 매니저와 Spring의 트랜잭션 관리가 필요합니다.
 */
@Service
@Transactional
public class ClassTransactionalExample {

    private final MethodTransactionalExample methodTransactionalExample;

    public ClassTransactionalExample(MethodTransactionalExample methodTransactionalExample) {
        this.methodTransactionalExample = methodTransactionalExample;
    }

    // 사용 흐름: 다른 Spring 빈에서 이 빈을 주입받아 placeOrder()를 호출합니다.
    // 트랜잭션 구성 시: 시작 → 주문 처리 → 결제 처리 → 정상 종료 시 커밋.
    // RuntimeException이 밖으로 전파되면 기본 설정에서는 롤백됩니다.
    public void placeOrder() {
        System.out.println("1. 주문 처리 (DB 작업 생략)");
        methodTransactionalExample.processPayment();
        System.out.println("3. 주문 완료");
    }

    // 메소드에 @Transactional이 없어도 클래스의 설정을 따릅니다.
    public void cancelOrder() {
        System.out.println("주문 취소 (DB 작업 생략)");
    }
}
