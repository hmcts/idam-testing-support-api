package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

@Slf4j
@Service
public class AdminService {

    @Value("${cleanup.burner.lifespan}")
    private Duration burnerLifespan;

    @Value("${cleanup.session.lifespan}")
    private Duration sessionLifespan;

    private final TestingUserService testingUserService;

    private final TestingRoleService testingRoleService;

    private final TestingServiceProviderService testingServiceProviderService;

    private final TestingSessionService testingSessionService;

    private Clock clock;

    public AdminService(TestingUserService testingUserService, TestingRoleService testingRoleService,
                        TestingServiceProviderService testingServiceProviderService,
                        TestingSessionService testingSessionService) {
        this.testingUserService = testingUserService;
        this.testingRoleService = testingRoleService;
        this.testingServiceProviderService = testingServiceProviderService;
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

        ZonedDateTime expiryTime = ZonedDateTime.now(clock).minus(sessionLifespan);

        triggerRemoveDependencySessionExpiry(expiryTime);
        triggerActiveSessionExpiry(expiryTime);

    }

    protected void triggerActiveSessionExpiry(ZonedDateTime expiryTime) {
        List<TestingSession> expiredSessions = testingSessionService
            .getExpiredSessionsByState(expiryTime, TestingState.ACTIVE);
        if (CollectionUtils.isNotEmpty(expiredSessions)) {
            for (TestingSession expiredSession : expiredSessions) {
                List<TestingEntity> sessionUsers = testingUserService.getTestingEntitiesForSession(expiredSession);
                if (CollectionUtils.isNotEmpty(sessionUsers)) {

                    expiredSession.setState(TestingState.REMOVE_DEPENDENCIES);
                    expiredSession.setLastModifiedDate(ZonedDateTime.now(clock));
                    testingSessionService.updateSession(expiredSession);

                    for (TestingEntity sessionUser : sessionUsers) {
                        testingUserService.requestCleanup(sessionUser);
                    }
                    log.info(
                        "Changed session '{}' with key '{}' from {} to {}",
                        expiredSession.getId(),
                        expiredSession.getSessionKey(),
                        TestingState.ACTIVE,
                        expiredSession.getState()
                    );

                } else {
                    testingSessionService.requestCleanup(expiredSession);
                    log.info("Requested cleanup of active session {}", expiredSession.getId());
                }
            }
        } else {
            log.info("No expired active sessions");
        }
    }

    protected void triggerRemoveDependencySessionExpiry(ZonedDateTime expiryTime) {
        List<TestingSession> expiredSessions = testingSessionService
            .getExpiredSessionsByState(expiryTime, TestingState.REMOVE_DEPENDENCIES);
        if (CollectionUtils.isNotEmpty(expiredSessions)) {
            for (TestingSession expiredSession : expiredSessions) {
                List<TestingEntity> sessionUsers = testingUserService.getTestingEntitiesForSession(expiredSession);
                if (CollectionUtils.isEmpty(sessionUsers)) {
                    testingSessionService.requestCleanup(expiredSession);
                    log.info("Requested cleanup of session {} after dependency cleanup", expiredSession.getId());
                } else {
                    log.info(
                        "Session {} still has {} user testing entities",
                        expiredSession.getId(),
                        CollectionUtils.size(sessionUsers)
                    );
                }
            }
        } else {
            log.info("No expired remove dependency sessions");
        }
    }

    public void cleanupUser(CleanupEntity userEntity) {
        if (testingUserService.delete(userEntity.getEntityId())) {
            log.info("Deleted user {}", userEntity.getEntityId());
        } else {
            log.info("No user found for id {}", userEntity.getEntityId());
        }
        testingUserService.deleteTestingEntityById(userEntity.getTestingEntityId());
    }

    public void cleanupSession(CleanupSession session) {

        List<TestingEntity> sessionRoles = testingRoleService
            .getTestingEntitiesForSessionById(session.getTestingSessionId());
        if (CollectionUtils.isNotEmpty(sessionRoles)) {
            for (TestingEntity sessionRole : sessionRoles) {
                testingRoleService.requestCleanup(sessionRole);
                log.info("request role cleanup {}", sessionRole.getEntityId());
            }
        }
        List<TestingEntity> sessionServices = testingServiceProviderService
            .getTestingEntitiesForSessionById(session.getTestingSessionId());
        if (CollectionUtils.isNotEmpty(sessionServices)) {
            for (TestingEntity sessionService : sessionServices) {
                testingServiceProviderService.requestCleanup(sessionService);
                log.info("request service cleanup {}", sessionService.getEntityId());
            }
        }

        log.info("Removing session {}", session.getTestingSessionId());
        testingSessionService.deleteSession(session.getTestingSessionId());
    }

    public void cleanupRole(CleanupEntity roleEntity) {
        if (testingRoleService.delete(roleEntity.getEntityId())) {
            log.info("Deleted role {}", roleEntity.getEntityId());
        } else {
            log.info("No role found for name {}", roleEntity.getEntityId());
        }
        testingRoleService.deleteTestingEntityById(roleEntity.getTestingEntityId());
    }

    public void cleanupService(CleanupEntity serviceEntity) {
        if (testingServiceProviderService.delete(serviceEntity.getEntityId())) {
            log.info("Deleted service {}", serviceEntity.getEntityId());
        } else {
            log.info("No service found for client id {}", serviceEntity.getEntityId());
        }
        testingServiceProviderService.deleteTestingEntityById(serviceEntity.getTestingEntityId());
    }

}
