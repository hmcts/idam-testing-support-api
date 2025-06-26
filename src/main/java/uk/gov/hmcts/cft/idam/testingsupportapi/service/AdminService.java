package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import com.google.common.annotations.VisibleForTesting;
import io.opentelemetry.api.trace.Span;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cft.idam.api.v2.common.ratelimit.RateLimitService;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingState;
import uk.gov.hmcts.cft.idam.testingsupportapi.trace.TraceAttribute;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static uk.gov.hmcts.cft.idam.api.v2.common.ratelimit.RateLimitService.RateLimitServiceOutcome.TOO_MANY_REQUESTS;
import static uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingState.REMOVE_DEPENDENCIES;

@Slf4j
@Service
public class AdminService {
    private final TestingUserService testingUserService;
    private final TestingRoleService testingRoleService;
    private final TestingServiceProviderService testingServiceProviderService;
    private final TestingSessionService testingSessionService;
    private final TestingUserProfileService testingUserProfileService;
    private final TestingCaseWorkerProfileService testingCaseWorkerProfileService;
    private final RateLimitService burnerExpiryRateLimitService;
    private final TestingInvitationService testingInvitationService;

    public static final String DELETED = "deleted";
    public static final String NOT_FOUND = "not-found";

    @Value("${cleanup.burner.lifespan}")
    private Duration burnerLifespan;
    @Value("${cleanup.session.lifespan}")
    private Duration sessionLifespan;
    private Clock clock;

    public AdminService(TestingUserService testingUserService,
                        TestingRoleService testingRoleService,
                        TestingServiceProviderService testingServiceProviderService,
                        TestingSessionService testingSessionService,
                        TestingUserProfileService testingUserProfileService,
                        TestingCaseWorkerProfileService testingCaseWorkerProfileService,
                        RateLimitService burnerExpiryRateLimitService,
                        TestingInvitationService testingInvitationService) {
        this.testingUserService = testingUserService;
        this.testingRoleService = testingRoleService;
        this.testingServiceProviderService = testingServiceProviderService;
        this.testingSessionService = testingSessionService;
        this.testingUserProfileService = testingUserProfileService;
        this.testingCaseWorkerProfileService = testingCaseWorkerProfileService;
        this.burnerExpiryRateLimitService = burnerExpiryRateLimitService;
        this.testingInvitationService = testingInvitationService;
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

        if (burnerExpiryRateLimitService.rateLimitByBucket("burner-expiry") == TOO_MANY_REQUESTS) {
            log.info("Burner user cleanup already in progress");
            return;
        }

        ZonedDateTime now = ZonedDateTime.now(clock);

        List<TestingEntity> burnerEntities = testingUserService.getExpiredBurnerUserTestingEntities(now.minus(
            burnerLifespan));
        if (CollectionUtils.isNotEmpty(burnerEntities)) {
            Span.current().setAttribute(TraceAttribute.COUNT, String.valueOf(burnerEntities.size()));
            for (TestingEntity burnerEntity : burnerEntities) {
                log.info("Requesting cleanup for burner user {}", burnerEntity.getEntityId());
                testingUserService.requestCleanup(burnerEntity);
            }
        } else {
            Span.current().setAttribute(TraceAttribute.COUNT, "0");
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
        List<TestingSession> expiredSessions = testingSessionService.getExpiredSessionsByState(
            expiryTime,
            TestingState.ACTIVE
        );
        if (CollectionUtils.isNotEmpty(expiredSessions)) {
            Span.current().setAttribute(TraceAttribute.COUNT, String.valueOf(expiredSessions.size()));
            for (TestingSession expiredSession : expiredSessions) {
                List<TestingEntity> sessionUsers = testingUserService.getTestingEntitiesForSession(expiredSession);
                List<TestingEntity> sessionProfiles = testingUserProfileService.getTestingEntitiesForSession(
                    expiredSession);
                List<TestingEntity> sessionCaseworkers = testingCaseWorkerProfileService.getTestingEntitiesForSession(
                    expiredSession);
                if (CollectionUtils.isNotEmpty(sessionUsers) || CollectionUtils.isNotEmpty(sessionProfiles)
                    || CollectionUtils.isNotEmpty(sessionCaseworkers)) {

                    expiredSession.setState(REMOVE_DEPENDENCIES);
                    expiredSession.setLastModifiedDate(ZonedDateTime.now(clock));
                    testingSessionService.updateSession(expiredSession);

                    sessionUsers.forEach(testingUserService::requestCleanup);
                    sessionProfiles.forEach(testingUserProfileService::requestCleanup);
                    sessionCaseworkers.forEach(testingCaseWorkerProfileService::requestCleanup);

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
            Span.current().setAttribute(TraceAttribute.COUNT, "0");
        }
    }

    protected void triggerRemoveDependencySessionExpiry(ZonedDateTime expiryTime) {
        List<TestingSession> expiredSessions = testingSessionService.getExpiredSessionsByState(
            expiryTime,
            REMOVE_DEPENDENCIES
        );
        if (CollectionUtils.isNotEmpty(expiredSessions)) {
            Span.current().setAttribute(TraceAttribute.COUNT, String.valueOf(expiredSessions.size()));
            for (TestingSession expiredSession : expiredSessions) {
                List<TestingEntity> sessionUsers = testingUserService.getTestingEntitiesForSession(expiredSession);
                List<TestingEntity> sessionProfiles = testingUserProfileService.getTestingEntitiesForSession(
                    expiredSession);
                List<TestingEntity> sessionCaseworkers = testingCaseWorkerProfileService.getTestingEntitiesForSession(
                    expiredSession);
                if (CollectionUtils.isEmpty(sessionUsers) && CollectionUtils.isEmpty(sessionProfiles)
                    && CollectionUtils.isEmpty(sessionCaseworkers)) {
                    testingSessionService.requestCleanup(expiredSession);
                    log.info("Requested cleanup of session {} after dependency cleanup", expiredSession.getId());
                } else {
                    log.info(
                        "Session {} still has testing entities, {} user(s), {} user-profile(s), {} caseworker-profile"
                            + "(s)",
                        expiredSession.getId(),
                        CollectionUtils.size(sessionUsers),
                        CollectionUtils.size(sessionProfiles),
                        CollectionUtils.size(sessionCaseworkers)
                    );
                }
            }
        } else {
            Span.current().setAttribute(TraceAttribute.COUNT, "0");
        }
    }

    public void cleanupSession(CleanupSession session) {

        List<TestingEntity> sessionRoles =
            testingRoleService.getTestingEntitiesForSessionById(session.getTestingSessionId());
        if (CollectionUtils.isNotEmpty(sessionRoles)) {
            for (TestingEntity sessionRole : sessionRoles) {
                testingRoleService.requestCleanup(sessionRole);
                log.info(
                    "request role cleanup {}, session id {}",
                    sessionRole.getEntityId(),
                    sessionRole.getTestingSessionId()
                );
            }
        }
        List<TestingEntity> sessionServices =
            testingServiceProviderService.getTestingEntitiesForSessionById(session.getTestingSessionId());
        if (CollectionUtils.isNotEmpty(sessionServices)) {
            for (TestingEntity sessionService : sessionServices) {
                testingServiceProviderService.requestCleanup(sessionService);
                log.info(
                    "request service cleanup {}, session id {}",
                    sessionService.getEntityId(),
                    sessionService.getTestingSessionId()
                );
            }
        }

        log.info("Removing session {}", session.getTestingSessionId());
        testingSessionService.deleteSession(session.getTestingSessionId());
    }

    public void cleanupUser(CleanupEntity userEntity) {
        if (skipCleanupForRecentUserLogin(userEntity)) {
            testingUserService.detachEntity(userEntity.getTestingEntityId());
        } else {
            testingUserService.doCleanup(userEntity);
        }
    }

    protected boolean skipCleanupForRecentUserLogin(CleanupEntity entity) {
        if (TestingUserService.UserCleanupStrategy.SKIP_RECENT_LOGINS == testingUserService.getUserCleanupStrategy()
            && testingUserService.isRecentLogin(entity.getEntityId())) {
            Span.current().setAttribute(TraceAttribute.OUTCOME, "recent-login");
            return true;
        }
        return false;
    }

    public void cleanupRole(CleanupEntity roleEntity) {
        testingRoleService.doCleanup(roleEntity);
    }

    public void cleanupService(CleanupEntity serviceEntity) {
        testingServiceProviderService.doCleanup(serviceEntity);
    }

    public void cleanupUserProfile(CleanupEntity profileEntity) {
        if (skipCleanupForRecentUserLogin(profileEntity)) {
            testingUserProfileService.detachEntity(profileEntity.getTestingEntityId());
        } else {
            testingUserProfileService.doCleanup(profileEntity);
        }
    }

    public void cleanupCaseWorkerProfile(CleanupEntity profileEntity) {
        if (skipCleanupForRecentUserLogin(profileEntity)) {
            testingCaseWorkerProfileService.detachEntity(profileEntity.getTestingEntityId());
        } else {
            testingCaseWorkerProfileService.doCleanup(profileEntity);
        }
    }

    public void cleanupInvitation(CleanupEntity invitationEntity) {
        testingInvitationService.doCleanup(invitationEntity);
    }


}
