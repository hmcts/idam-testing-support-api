package uk.gov.hmcts.cft.idam.testingsupportapi.startup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingSessionRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingState;

import jakarta.transaction.Transactional;

@Slf4j
@Component
public class StartupEventListener {

    private final TestingSessionRepo testingSessionRepo;

    public StartupEventListener(TestingSessionRepo testingSessionRepo) {
        this.testingSessionRepo = testingSessionRepo;
    }

    @Transactional
    @EventListener
    public void resetSessionStateOnStartup(ContextRefreshedEvent event) {
        int changed = testingSessionRepo.updateAllSessionStates(TestingState.ACTIVE);
        log.info("Reset session state on startup for {} session(s)", changed);
    }
}
