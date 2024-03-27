package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper;
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
        when(testingUserService.deleteTestingEntityById("test-id")).thenReturn(true);
        when(testingUserService.delete("test-user-id")).thenReturn(true);
        underTest.cleanupUser(cleanupEntity);
        when(testingUserService.delete("test-user-id")).thenReturn(false);
        underTest.cleanupUser(cleanupEntity);
        verify(testingUserService, times(2)).deleteTestingEntityById("test-id");
    }

    @Test
    void cleanupUser_dormant() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        cleanupEntity.setTestingEntityId("test-id");
        cleanupEntity.setEntityId("test-user-id");
        when(testingUserService.getUserCleanupStrategy()).thenReturn(TestingUserService.UserCleanupStrategy.SKIP_RECENT_LOGINS);
        when(testingUserService.isRecentLogin(any())).thenReturn(true);
        underTest.cleanupUser(cleanupEntity);
        verify(testingUserService, times(1)).detachEntity("test-id");
        verify(testingUserService, never()).deleteTestingEntityById(any());
        verify(testingUserService, never()).delete(any());
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
        when(testingRoleService.deleteTestingEntityById("test-id")).thenReturn(true);
        when(testingRoleService.delete("test-role-name")).thenReturn(true);
        underTest.cleanupRole(cleanupEntity);
        when(testingRoleService.delete("test-role-name")).thenReturn(false);
        underTest.cleanupRole(cleanupEntity);
        verify(testingRoleService, times(2)).deleteTestingEntityById("test-id");
    }

    @Test
    void cleanupService() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        cleanupEntity.setTestingEntityId("test-id");
        cleanupEntity.setEntityId("test-service-client");
        when(testingServiceProviderService.deleteTestingEntityById("test-id")).thenReturn(true);
        when(testingServiceProviderService.delete("test-service-client")).thenReturn(true);
        underTest.cleanupService(cleanupEntity);
        when(testingServiceProviderService.delete("test-service-client")).thenReturn(false);
        underTest.cleanupService(cleanupEntity);
        verify(testingServiceProviderService, times(2)).deleteTestingEntityById("test-id");
    }

    @Test
    void cleanupUserProfile() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        cleanupEntity.setTestingEntityId("test-id");
        cleanupEntity.setEntityId("test-user-profile-id");
        when(testingUserProfileService.deleteTestingEntityById("test-id")).thenReturn(true);
        when(testingUserProfileService.delete("test-user-profile-id")).thenReturn(true);
        underTest.cleanupUserProfile(cleanupEntity);
        when(testingUserProfileService.delete("test-user-profile-id")).thenReturn(false);
        underTest.cleanupUserProfile(cleanupEntity);
        verify(testingUserProfileService, times(2)).deleteTestingEntityById("test-id");
    }

    @Test
    void cleanupUserProfile_detach() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        cleanupEntity.setTestingEntityId("test-id");
        cleanupEntity.setEntityId("test-user-profile-id");
        when(testingUserProfileService.delete("test-user-profile-id")).thenThrow(SpringWebClientHelper.internalServierError());
        underTest.cleanupUserProfile(cleanupEntity);
        verify(testingUserProfileService, times(1)).detachEntity("test-id");
        verify(testingUserProfileService, never()).deleteTestingEntityById("test-id");
    }

    @Test
    void cleanupCaseWorkerProfile() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        cleanupEntity.setTestingEntityId("test-id");
        cleanupEntity.setEntityId("test-caseworker-profile-id");
        when(testingCaseWorkerProfileService.deleteTestingEntityById("test-id")).thenReturn(true);
        when(testingCaseWorkerProfileService.delete("test-caseworker-profile-id")).thenReturn(true);
        underTest.cleanupCaseWorkerProfile(cleanupEntity);
        when(testingCaseWorkerProfileService.delete("test-caseworker-profile-id")).thenReturn(false);
        underTest.cleanupCaseWorkerProfile(cleanupEntity);
        verify(testingCaseWorkerProfileService, times(2)).deleteTestingEntityById("test-id");
    }

    @Test
    void cleanupCaseWorkerProfile_detach() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        cleanupEntity.setTestingEntityId("test-id");
        cleanupEntity.setEntityId("test-caseworker-profile-id");
        when(testingCaseWorkerProfileService.delete("test-caseworker-profile-id")).thenThrow(SpringWebClientHelper.internalServierError());
        underTest.cleanupCaseWorkerProfile(cleanupEntity);
        verify(testingCaseWorkerProfileService, times(1)).detachEntity("test-id");
        verify(testingCaseWorkerProfileService, never()).deleteTestingEntityById("test-id");
    }
}
