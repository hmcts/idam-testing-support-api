package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingState;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AdminService {

    @Value("${cleanup.burner.lifespan}")
    private Duration burnerLifespan;

    @Value("${cleanup.session.lifespan}")
    private Duration sessionLifespan;

    private final TestingUserService testingUserService;

    private final TestingSessionService testingSessionService;

    private Clock clock;

    public AdminService(TestingUserService testingUserService, TestingSessionService testingSessionService) {
        this.testingUserService = testingUserService;
        this.testingSessionService = testingSessionService;
        this.clock = Clock.system(ZoneOffset.UTC);
    }

    @VisibleForTesting
    protected void changeClock(Clock clock) {
        this.clock = clock;
    }

    @VisibleForTesting
    protected void setBurnerLifespan(Duration duration) {
        this.burnerLifespan = duration;
    }

    @VisibleForTesting
    protected void setSessionLifespan(Duration duration) {
        this.sessionLifespan = duration;
    }

    /**
     * Trigger expiry for burner users.
     *
     * @should
     */
    public void triggerExpiryBurnerUsers() {

        ZonedDateTime now = ZonedDateTime.now(clock);

        List<TestingEntity> burnerEntities = testingUserService
            .getExpiredBurnerUserTestingEntities(now.minus(burnerLifespan));
        if (CollectionUtils.isNotEmpty(burnerEntities)) {
            log.info("Found {} burner user(s)", burnerEntities.size());
            for (TestingEntity burnerEntity : burnerEntities) {
                testingUserService.requestCleanup(burnerEntity);
            }
        } else {
            log.info("No burner users to remove");
        }

    }

    /**
     * Trigger expiry for sessions.
     */
    public void triggerExpirySessions() {

        ZonedDateTime now = ZonedDateTime.now(clock);

        List<TestingSession> expiredSessions = testingSessionService.getExpiredSessions(now.minus(sessionLifespan));
        if (CollectionUtils.isNotEmpty(expiredSessions)) {
            log.info("Found {} expired session(s)", expiredSessions.size());

            for (TestingSession expiredSession : expiredSessions) {
                List<TestingEntity> sessionUsers = testingUserService.getUsersForSession(expiredSession);
                if (CollectionUtils.isNotEmpty(sessionUsers) && expiredSession.getState() == TestingState.ACTIVE) {

                    final TestingState originalState = expiredSession.getState();
                    expiredSession.setState(TestingState.REMOVE_DEPENDENCIES);
                    expiredSession.setLastModifiedDate(ZonedDateTime.now(clock));
                    testingSessionService.updateSession(expiredSession);

                    for (TestingEntity sessionUser : sessionUsers) {
                        testingUserService.requestCleanup(sessionUser);
                    }
                    log.info(
                        "Changed session {} from {} to {}",
                        expiredSession.getId(),
                        originalState,
                        expiredSession.getState()
                    );

                } else if (CollectionUtils.isEmpty(sessionUsers) && expiredSession.getState() != TestingState.REMOVE) {

                    testingSessionService.requestCleanup(expiredSession);
                    log.info("Removed session {}", expiredSession.getId());

                } else {
                    log.info("Session {} still has {} user entities, state is {}",
                             expiredSession.getId(),
                             sessionUsers.size(),
                             expiredSession.getState()
                    );
                }
            }

        } else {
            log.info("No expired sessions");
        }

    }

    public void cleanupUser(CleanupEntity userEntity) {
        Optional<User> user = testingUserService.deleteIdamUserIfPresent(userEntity.getEntityId());
        if (user.isPresent()) {
            log.info("Deleted user {}", userEntity.getEntityId());
        } else {
            log.info("No user found for id {}", userEntity.getEntityId());
        }
        testingUserService.deleteTestingEntityById(userEntity.getTestingEntityId());
    }

    public void cleanupSession(CleanupSession session) {
        testingSessionService.deleteSession(session.getTestingSessionId());
    }

}
