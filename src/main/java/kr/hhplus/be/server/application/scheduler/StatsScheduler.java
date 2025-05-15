package kr.hhplus.be.server.application.scheduler;

import kr.hhplus.be.server.domain.stats.StatsCommand;
import kr.hhplus.be.server.domain.stats.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class StatsScheduler {

    private final StatsService salesService;

    @Scheduled(cron = "0 0 0 * * *")
    public void hourlySalesProducts() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime today = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth() - 1,
                0, 0, 0);
        StatsCommand.SaveSalesProducts command = new StatsCommand.SaveSalesProducts(today);
        salesService.saveSalesProductByDateTime(command);
    }

    @Scheduled(cron = "0 30 23 * * *")
    public void dailySalesProducts() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime today = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth() - 1,
                0, 0, 0);
        StatsCommand.SaveSalesProducts command = new StatsCommand.SaveSalesProducts(today);
        salesService.saveSalesProductByDateTime(command);
    }

}
