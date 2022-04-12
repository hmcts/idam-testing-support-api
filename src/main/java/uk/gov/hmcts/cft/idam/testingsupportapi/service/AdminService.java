package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSessionState;

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

    public AdminService(TestingUserService testingUserService,
                        TestingSessionService testingSessionService) {
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
     * Trigger expiry for burner users
     * @should
     */
    public void triggerExpiryBurnerUsers() {

        ZonedDateTime now = ZonedDateTime.now(clock);

        // check for burner users to remove
        List<TestingEntity> burnerEntities = testingUserService.getExpiredBurnerUserTestingEntities(now.minus(burnerLifespan));
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

        // check for sessions that have expired
        List<TestingSession> expiredSessions = testingSessionService.getExpiredSessions(now.minus(sessionLifespan));
        if (CollectionUtils.isNotEmpty(expiredSessions)) {
            log.info("Found {} expired session(s)", expiredSessions.size());

            for (TestingSession expiredSession : expiredSessions) {
                List<TestingEntity> sessionUsers = testingUserService.getUsersForSession(expiredSession);
                if (CollectionUtils.isNotEmpty(sessionUsers) && expiredSession.getState() != TestingSessionState.EXPIRED) {

                    TestingSessionState originalState = expiredSession.getState();
                    expiredSession.setState(TestingSessionState.EXPIRED);
                    testingSessionService.updateSession(expiredSession);

                    for (TestingEntity sessionUser : sessionUsers) {
                        testingUserService.requestCleanup(sessionUser);
                    }
                    log.info("Set session {} to expired (was {})", expiredSession.getId(), originalState);

                } else if (CollectionUtils.isEmpty(sessionUsers)) {

                    testingSessionService.deleteSession(expiredSession);
                    log.info("Removed session {}", expiredSession.getId());

                } else {
                    log.info("Session {} still has {} user entities, state is {}",
                             expiredSession.getId(), sessionUsers.size(), expiredSession.getState());
                }

            }

        } else {
            log.info("No expired sessions");
        }

    }

    /**
     * Delete user for testing entity.
     */
    public void deleteUser(TestingEntity testingEntity) {
        Optional<User> user = testingUserService.deleteIdamUserIfPresent(testingEntity);
        if (user.isPresent()) {
            log.info("Deleted user {}", testingEntity.getEntityId());
        } else {
            log.info("No user found for id {}", testingEntity.getEntityId());
        }
    }

}
