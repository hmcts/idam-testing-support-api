package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import uk.gov.hmcts.cft.idam.api.v1.usermanagement.IdamV1UserManagementApi;
import uk.gov.hmcts.cft.idam.api.v1.usermanagement.model.User;
import uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper;
import uk.gov.hmcts.cft.idam.api.v2.common.ratelimit.RateLimitService;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingState;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    private static final long EPOCH_1AM = 3600;

    @Mock
    TestingUserService testingUserService;

    @Mock
    TestingRoleService testingRoleService;

    @Mock
    TestingServiceProviderService testingServiceProviderService;

    @Mock
    TestingSessionService testingSessionService;

    @Mock
    TestingUserProfileService testingUserProfileService;

    @Mock
    TestingCaseWorkerProfileService testingCaseWorkerProfileService;

    @Mock
    RateLimitService burnerExpiryRateLimitService;

    @Mock
    IdamV1UserManagementApi idamV1UserManagementApi;

    @InjectMocks
    AdminService underTest;

    @BeforeEach
    public void setup() {
        underTest.changeClock(Clock.fixed(Instant.ofEpochSecond(EPOCH_1AM), ZoneOffset.UTC));
        underTest.setBurnerLifespan(Duration.of(1, ChronoUnit.MINUTES));
        underTest.setSessionLifespan(Duration.of(2, ChronoUnit.MINUTES));
    }

    @Test
    void triggerExpiryBurnerUsers_oneBurnerUserSuccess() {
        TestingEntity testingEntity = new TestingEntity();
        when(testingUserService.getExpiredBurnerUserTestingEntities(any())).thenReturn(Collections.singletonList(
            testingEntity));
        underTest.triggerExpiryBurnerUsers();
        verify(testingUserService, times(1)).requestCleanup(testingEntity);
    }

    @Test
    void triggerExpiryBurnerUsers_rateLimitBlocked() {
        when(burnerExpiryRateLimitService.rateLimitByBucket(any())).thenReturn(RateLimitService.RateLimitServiceOutcome.TOO_MANY_REQUESTS);
        underTest.triggerExpiryBurnerUsers();
        verify(testingUserService, never()).getExpiredBurnerUserTestingEntities(any());
        verify(testingUserService, never()).requestCleanup(any());
    }

    @Test
    void triggerExpirySessions_oneSessionWithOneUser() {
        TestingSession testingSession = new TestingSession();
        testingSession.setState(TestingState.ACTIVE);
        TestingEntity testingEntity = new TestingEntity();
        when(testingSessionService.getExpiredSessionsByState(
            any(),
            eq(TestingState.ACTIVE)
        )).thenReturn(Collections.singletonList(testingSession));
        when(testingSessionService.getExpiredSessionsByState(any(), eq(TestingState.REMOVE_DEPENDENCIES))).thenReturn(
            Collections.emptyList());
        when(testingUserService.getTestingEntitiesForSession(any())).thenReturn(Collections.singletonList(testingEntity));
        underTest.triggerExpirySessions();

        verify(testingSessionService, times(1)).updateSession(eq(testingSession));
        verify(testingUserService, times(1)).requestCleanup(testingEntity);
    }

    @Test
    void triggerExpirySessions_oneSessionWithOneUserProfile() {
        TestingSession testingSession = new TestingSession();
        testingSession.setState(TestingState.ACTIVE);
        TestingEntity testingEntity = new TestingEntity();
        when(testingSessionService.getExpiredSessionsByState(
            any(),
            eq(TestingState.ACTIVE)
        )).thenReturn(Collections.singletonList(testingSession));
        when(testingSessionService.getExpiredSessionsByState(any(), eq(TestingState.REMOVE_DEPENDENCIES))).thenReturn(
            Collections.emptyList());
        when(testingUserService.getTestingEntitiesForSession(any())).thenReturn(Collections.emptyList());
        when(testingUserProfileService.getTestingEntitiesForSession(any())).thenReturn(Collections.singletonList(
            testingEntity));
        underTest.triggerExpirySessions();

        verify(testingSessionService, times(1)).updateSession(eq(testingSession));
        verify(testingUserService, never()).requestCleanup(testingEntity);
        verify(testingUserProfileService, times(1)).requestCleanup(testingEntity);
    }

    @Test
    void triggerExpirySessions_oneSessionWithOneCaseWorkerProfile() {
        TestingSession testingSession = new TestingSession();
        testingSession.setState(TestingState.ACTIVE);
        TestingEntity testingEntity = new TestingEntity();
        when(testingSessionService.getExpiredSessionsByState(
            any(),
            eq(TestingState.ACTIVE)
        )).thenReturn(Collections.singletonList(testingSession));
        when(testingSessionService.getExpiredSessionsByState(any(), eq(TestingState.REMOVE_DEPENDENCIES))).thenReturn(
            Collections.emptyList());
        when(testingUserService.getTestingEntitiesForSession(any())).thenReturn(Collections.emptyList());
        when(testingUserProfileService.getTestingEntitiesForSession(any())).thenReturn(Collections.emptyList());
        when(testingCaseWorkerProfileService.getTestingEntitiesForSession(any())).thenReturn(List.of(testingEntity));

        underTest.triggerExpirySessions();

        verify(testingSessionService, times(1)).updateSession(eq(testingSession));
        verify(testingUserService, never()).requestCleanup(testingEntity);
        verify(testingUserProfileService, never()).requestCleanup(testingEntity);
        verify(testingCaseWorkerProfileService, times(1)).requestCleanup(testingEntity);

    }

    @Test
    void triggerExpirySessions_oneSessionNoUsers() {
        TestingSession testingSession = new TestingSession();
        testingSession.setState(TestingState.ACTIVE);
        when(testingSessionService.getExpiredSessionsByState(
            any(),
            eq(TestingState.ACTIVE)
        )).thenReturn(Collections.singletonList(testingSession));
        when(testingSessionService.getExpiredSessionsByState(any(), eq(TestingState.REMOVE_DEPENDENCIES))).thenReturn(
            Collections.emptyList());

        when(testingUserService.getTestingEntitiesForSession(any())).thenReturn(Collections.emptyList());
        underTest.triggerExpirySessions();

        verify(testingSessionService, times(1)).requestCleanup(eq(testingSession));
        verify(testingSessionService, never()).updateSession(eq(testingSession));
        verify(testingUserService, never()).requestCleanup(any());
    }

    @Test
    void triggerExpirySessions_oneRemoveDependenciesSessionWithNoUsers() {
        TestingSession testingSession = new TestingSession();
        testingSession.setState(TestingState.REMOVE_DEPENDENCIES);
        when(testingUserService.getTestingEntitiesForSession(any())).thenReturn(Collections.emptyList());
        when(testingSessionService.getExpiredSessionsByState(
            any(),
            eq(TestingState.ACTIVE)
        )).thenReturn(Collections.emptyList());
        when(testingSessionService.getExpiredSessionsByState(any(), eq(TestingState.REMOVE_DEPENDENCIES))).thenReturn(
            Collections.singletonList(testingSession));

        underTest.triggerExpirySessions();

        verify(testingSessionService, times(1)).requestCleanup(eq(testingSession));
        verify(testingSessionService, never()).updateSession(eq(testingSession));
        verify(testingUserService, never()).requestCleanup(any());
    }

    @Test
    void triggerExpirySessions_oneRemoveDependenciesSessionWithOneUser() {
        TestingSession testingSession = new TestingSession();
        testingSession.setState(TestingState.REMOVE_DEPENDENCIES);
        TestingEntity testingEntity = new TestingEntity();
        when(testingUserService.getTestingEntitiesForSession(any())).thenReturn(Collections.singletonList(testingEntity));
        when(testingSessionService.getExpiredSessionsByState(
            any(),
            eq(TestingState.ACTIVE)
        )).thenReturn(Collections.emptyList());
        when(testingSessionService.getExpiredSessionsByState(any(), eq(TestingState.REMOVE_DEPENDENCIES))).thenReturn(
            Collections.singletonList(testingSession));

        underTest.triggerExpirySessions();

        verify(testingSessionService, never()).requestCleanup(eq(testingSession));
        verify(testingSessionService, never()).updateSession(eq(testingSession));
        verify(testingUserService, never()).requestCleanup(any());
    }

    @Test
    void triggerExpirySessions_oneRemoveDependenciesSessionWithOneUserProfile() {
        TestingSession testingSession = new TestingSession();
        testingSession.setState(TestingState.REMOVE_DEPENDENCIES);
        TestingEntity testingEntity = new TestingEntity();
        when(testingUserService.getTestingEntitiesForSession(any())).thenReturn(Collections.emptyList());
        when(testingUserProfileService.getTestingEntitiesForSession(any())).thenReturn(Collections.singletonList(
            testingEntity));
        when(testingSessionService.getExpiredSessionsByState(
            any(),
            eq(TestingState.ACTIVE)
        )).thenReturn(Collections.emptyList());
        when(testingSessionService.getExpiredSessionsByState(any(), eq(TestingState.REMOVE_DEPENDENCIES))).thenReturn(
            Collections.singletonList(testingSession));

        underTest.triggerExpirySessions();

        verify(testingSessionService, never()).requestCleanup(eq(testingSession));
        verify(testingSessionService, never()).updateSession(eq(testingSession));
        verify(testingUserService, never()).requestCleanup(any());
        verify(testingUserProfileService, never()).requestCleanup(any());

    }

    @Test
    void cleanupUser() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        cleanupEntity.setTestingEntityId("test-id");
        cleanupEntity.setEntityId("test-user-id");
        underTest.cleanupUser(cleanupEntity);
        verify(testingUserService, times(1)).doCleanup(cleanupEntity);
    }

    @Test
    void cleanupUser_dormant() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        cleanupEntity.setTestingEntityId("test-id");
        cleanupEntity.setEntityId("test-user-id");
        when(testingUserService.getUserCleanupStrategy()).thenReturn(TestingUserService.UserCleanupStrategy.SKIP_RECENT_LOGINS);
        when(testingUserService.isRecentLogin(any())).thenReturn(true);
        underTest.cleanupUser(cleanupEntity);
        verify(testingUserService, never()).doCleanup(cleanupEntity);
        verify(testingUserService).detachEntity("test-id");

    }

    @Test
    void cleanupSession() {
        CleanupSession cleanupSession = new CleanupSession();
        cleanupSession.setTestingSessionId("test-session-id");
        underTest.cleanupSession(cleanupSession);
        verify(testingSessionService, times(1)).deleteSession("test-session-id");
    }

    @Test
    void cleanupSessionWithRoles() {
        CleanupSession cleanupSession = new CleanupSession();
        cleanupSession.setTestingSessionId("test-session-id");
        TestingEntity testingEntity = new TestingEntity();
        when(testingRoleService.getTestingEntitiesForSessionById("test-session-id")).thenReturn(Collections.singletonList(
            testingEntity));
        underTest.cleanupSession(cleanupSession);
        verify(testingSessionService, times(1)).deleteSession("test-session-id");
        verify(testingRoleService, times(1)).requestCleanup(testingEntity);
        verify(testingServiceProviderService, never()).requestCleanup(any());
    }

    @Test
    void cleanupSessionWithServices() {
        CleanupSession cleanupSession = new CleanupSession();
        cleanupSession.setTestingSessionId("test-session-id");
        TestingEntity testingEntity = new TestingEntity();
        when(testingServiceProviderService.getTestingEntitiesForSessionById("test-session-id")).thenReturn(Collections.singletonList(
            testingEntity));
        underTest.cleanupSession(cleanupSession);
        verify(testingSessionService, times(1)).deleteSession("test-session-id");
        verify(testingRoleService, never()).requestCleanup(any());
        verify(testingServiceProviderService, times(1)).requestCleanup(testingEntity);
    }

    @Test
    void cleanupRole() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        cleanupEntity.setTestingEntityId("test-id");
        cleanupEntity.setEntityId("test-role-name");
        underTest.cleanupRole(cleanupEntity);
        verify(testingRoleService, times(1)).doCleanup(cleanupEntity);
    }

    @Test
    void cleanupService() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        cleanupEntity.setTestingEntityId("test-id");
        cleanupEntity.setEntityId("test-service-client");
        underTest.cleanupService(cleanupEntity);
        verify(testingServiceProviderService, times(1)).doCleanup(cleanupEntity);
    }

    @Test
    void cleanupUserProfile() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        cleanupEntity.setTestingEntityId("test-id");
        cleanupEntity.setEntityId("test-user-profile-id");
        underTest.cleanupUserProfile(cleanupEntity);
        verify(testingUserProfileService, times(1)).doCleanup(cleanupEntity);
    }

    @Test
    void cleanupUserProfile_dormant() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        cleanupEntity.setTestingEntityId("test-id");
        cleanupEntity.setEntityId("test-user-profile-id");
        when(testingUserService.getUserCleanupStrategy()).thenReturn(TestingUserService.UserCleanupStrategy.SKIP_RECENT_LOGINS);
        when(testingUserService.isRecentLogin("test-user-profile-id")).thenReturn(true);
        underTest.cleanupUserProfile(cleanupEntity);
        verify(testingUserProfileService, never()).doCleanup(cleanupEntity);
        verify(testingUserProfileService).detachEntity("test-id");
    }

    @Test
    void cleanupCaseWorkerProfile() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        cleanupEntity.setTestingEntityId("test-id");
        cleanupEntity.setEntityId("test-caseworker-profile-id");
        underTest.cleanupCaseWorkerProfile(cleanupEntity);
        verify(testingCaseWorkerProfileService, times(1)).doCleanup(cleanupEntity);
    }

    @Test
    void cleanupCaseWorkerProfile_dormant() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        cleanupEntity.setTestingEntityId("test-id");
        cleanupEntity.setEntityId("test-caseworker-profile-id");
        when(testingUserService.getUserCleanupStrategy()).thenReturn(TestingUserService.UserCleanupStrategy.SKIP_RECENT_LOGINS);
        when(testingUserService.isRecentLogin("test-caseworker-profile-id")).thenReturn(true);
        underTest.cleanupCaseWorkerProfile(cleanupEntity);
        verify(testingCaseWorkerProfileService, never()).doCleanup(cleanupEntity);
        verify(testingCaseWorkerProfileService).detachEntity("test-id");
    }
}
