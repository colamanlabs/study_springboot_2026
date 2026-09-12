package com.example.beaninjection.example;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 메소드에 선언하면 해당 메소드에만 트랜잭션 설정이 적용됩니다.
 * DB 작업 대신 출력만 수행하므로 실제 데이터 커밋/롤백은 발생하지 않습니다.
 * 실제 트랜잭션 적용 시에는 다른 Spring 빈에서 주입받아 호출해야 합니다.
 * 같은 클래스 내부에서 직접 호출하면 기본 프록시 방식에서는 적용되지 않습니다.
 */
@Service
public class MethodTransactionalExample {

    // 기본 전파 속성 REQUIRED: 기존 트랜잭션이 있으면 참여하고, 없으면 새로 시작합니다.
    // 트랜잭션 구성 시 placeOrder()에서 호출하면 주문과 결제를 함께 처리합니다.
    @Transactional
    public void processPayment() {
        System.out.println("2. 결제 처리 (DB 작업 생략)");
    }

    // 이 메소드 자체는 트랜잭션을 시작하지 않습니다.
    // 호출자가 이미 트랜잭션을 시작했다면 그 실행 범위 안에서 동작합니다.
    public void printPaymentGuide() {
        System.out.println("결제 안내");
    }
}
