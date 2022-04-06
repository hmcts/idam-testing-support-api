package uk.gov.hmcts.cft.idam.testingsupportapi.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Service
public class AdminTask {

    private final LocalhostAdminApi localhostAdminApi;

    public AdminTask(LocalhostAdminApi localhostAdminApi) {
        this.localhostAdminApi = localhostAdminApi;
    }

    @Scheduled(initialDelayString = "${scheduler.initialDelayMs}",
        fixedRateString = "${scheduler.checkExpiry.frequencyMs}")
    public void checkExpiryTask() {
        log.info("I will trigger a check for expiry");
        localhostAdminApi.checkExpiry();
    }

    @FeignClient(name = "localhostAdminApi", url = "localhost:${server.port}")
    private interface LocalhostAdminApi {

        @GetMapping("/admin/check/expiry")
        void checkExpiry();

    }

}
