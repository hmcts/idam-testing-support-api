package uk.gov.hmcts.cft.idam.testingsupportapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import uk.gov.hmcts.cft.idam.testingsupportapi.internal.InternalAdminApi;

@Configuration
@EnableScheduling
@EnableAsync
@ConditionalOnProperty(name = "scheduler.enabled", matchIfMissing = true)
public class SchedulerConfig {

    @Autowired
    private InternalAdminApi internalAdminApi;

    @Scheduled(initialDelayString = "${scheduler.initialDelayMs}",
        fixedRateString = "${scheduler.burner.triggerExpiryFrequencyMs}")
    public void triggerExpiredBurnerUsersTask() {
        internalAdminApi.triggerExpiryBurnerUsers();
    }

    @Scheduled(initialDelayString = "${scheduler.initialDelayMs}",
        fixedRateString = "${scheduler.session.triggerExpiryFrequencyMs}")
    public void triggerExpiredSessionsTask() {
        internalAdminApi.triggerExpirySessions();
    }


}
