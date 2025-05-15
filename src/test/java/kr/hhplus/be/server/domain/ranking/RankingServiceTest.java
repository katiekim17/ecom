package kr.hhplus.be.server.domain.ranking;

import kr.hhplus.be.server.domain.order.OrderProduct;
import kr.hhplus.be.server.domain.product.Product;
import kr.hhplus.be.server.domain.product.ProductInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private RankingRepository rankingRepository;

    @InjectMocks
    private RankingService rankingService;

    @DisplayName("rankingService의 saveDailyRanking 호출되면 repository를 호출하여 해당 데이터를 저장한다.")
    @Test
    void saveSalesProductByOrder() {
        // given
        Product product = Product.create("사과", 1000, 10);
        List<OrderProduct> orderProducts = List.of(OrderProduct.create(ProductInfo.from(product), 2));
        LocalDateTime orderDateTime = LocalDateTime.of(2025, 5, 14, 0, 0, 0);
        RankingCommand.SaveDailyRanking command = new RankingCommand.SaveDailyRanking(orderProducts, orderDateTime);

        doNothing().when(rankingRepository).saveDailyRanking(orderProducts, orderDateTime);

        // when
        rankingService.saveDailyRanking(command);

        // then
        verify(rankingRepository, times(1)).saveDailyRanking(orderProducts, orderDateTime);
    }

}