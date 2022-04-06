package uk.gov.hmcts.cft.idam.testingsupportapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;

@Configuration
@EnableScheduling
@EnableAsync
@ConditionalOnProperty(name = "scheduler.enabled", matchIfMissing = true)
public class SchedulerConfig {

    @Autowired
    private LocalhostAdminApi localhostAdminApi;

    @Scheduled(initialDelayString = "${scheduler.initialDelayMs}",
        fixedRateString = "${scheduler.checkExpiry.frequencyMs}")
    public void checkExpiryTask() {
        localhostAdminApi.checkExpiry();
    }

    @FeignClient(name = "localhostAdminApi", url = "localhost:${server.port}")
    private interface LocalhostAdminApi {

        @GetMapping("/admin/check/expiry")
        void checkExpiry();

    }

}
