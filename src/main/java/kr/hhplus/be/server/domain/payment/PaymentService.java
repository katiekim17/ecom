package kr.hhplus.be.server.domain.payment;

import kr.hhplus.be.server.domain.point.PointCommand;
import kr.hhplus.be.server.domain.point.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PointService pointService;

    @Transactional
    public Payment pay(PaymentCommand command) {

        Payment payment = Payment.createByOrder(command.order());

        pointService.use(new PointCommand.USE(command.userId(), payment.getTotalAmount()));
        payment.complete(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

}
