package kr.hhplus.be.server.infra.payment;

import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepositoryImpl implements PaymentRepository {
    @Override
    public Payment save(Payment payment) {
        return null;
    }
}
