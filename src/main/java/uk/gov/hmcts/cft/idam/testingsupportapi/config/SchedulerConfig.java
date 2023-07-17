package uk.gov.hmcts.cft.idam.testingsupportapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.task.TaskSchedulerCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.ErrorHandler;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.AdminService;

@Configuration
@EnableScheduling
@EnableAsync
@ConditionalOnProperty(name = "scheduler.enabled", matchIfMissing = true)
public class SchedulerConfig implements TaskSchedulerCustomizer {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ErrorHandler schedulerErrorHandler;

    @Scheduled(initialDelayString = "${scheduler.initialDelayMs}",
        fixedRateString = "${scheduler.burner.triggerExpiryFrequencyMs}")
    public void triggerExpiredBurnerUsersTask() {
        adminService.triggerExpiryBurnerUsers();
    }

    @Scheduled(initialDelayString = "${scheduler.initialDelayMs}",
        fixedRateString = "${scheduler.session.triggerExpiryFrequencyMs}")
    public void triggerExpiredSessionsTask() {
        adminService.triggerExpirySessions();
    }

    @Override
    public void customize(ThreadPoolTaskScheduler taskScheduler) {
        taskScheduler.setErrorHandler(schedulerErrorHandler);
    }
}
