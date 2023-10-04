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
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingState;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.AdminService;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingUserService;

@Configuration
@EnableScheduling
@EnableAsync
@ConditionalOnProperty(name = "scheduler.enabled", matchIfMissing = true)
public class SchedulerConfig implements TaskSchedulerCustomizer {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ErrorHandler schedulerErrorHandler;

    @Autowired
    private TestingUserService testingUserService;

    @Scheduled(initialDelayString = "${scheduler.initialDelayMs}",
        fixedRateString = "${scheduler.burner.triggerExpiryFrequencyMs}")
    public void triggerExpiredBurnerUsersTask() {
        TestingEntity testingEntity = new TestingEntity();
        testingEntity.setEntityType(TestingEntityType.USER);
        testingEntity.setTestingSessionId("1234");
        testingEntity.setEntityId("1234");
        testingEntity.setId("1234");
        testingEntity.setState(TestingState.ACTIVE);
        testingUserService.requestCleanup(testingEntity);
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
