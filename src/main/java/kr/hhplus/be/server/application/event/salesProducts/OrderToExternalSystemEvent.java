package kr.hhplus.be.server.application.event.salesProducts;

import kr.hhplus.be.server.domain.order.OrderInfo;

/**
 * 주문 완료 후 외부 시스템(데이터 플랫폼 등)으로 주문/예약 정보를 전송하기 위한 이벤트
 */
public record OrderToExternalSystemEvent(
        OrderInfo orderInfo
) {
}
