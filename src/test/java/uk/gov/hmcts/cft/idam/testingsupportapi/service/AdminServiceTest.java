package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
        when(testingUserService.getExpiredBurnerUserTestingEntities(any()))
            .thenReturn(Collections.singletonList(testingEntity));
        underTest.triggerExpiryBurnerUsers();
        verify(testingUserService, times(1)).requestCleanup(eq(testingEntity));
    }

    @Test
    void triggerExpirySessions_oneSessionWithOneUser() {
        TestingSession testingSession = new TestingSession();
        testingSession.setState(TestingState.ACTIVE);
        TestingEntity testingEntity = new TestingEntity();
        when(testingSessionService.getExpiredSessionsByState(any(), eq(TestingState.ACTIVE))).thenReturn(Collections.singletonList(testingSession));
        when(testingSessionService.getExpiredSessionsByState(any(), eq(TestingState.REMOVE_DEPENDENCIES))).thenReturn(Collections.emptyList());
        when(testingUserService.getTestingEntitiesForSession(any())).thenReturn(Collections.singletonList(testingEntity));
        underTest.triggerExpirySessions();

        verify(testingSessionService, times(1)).updateSession(eq(testingSession));
        verify(testingUserService, times(1)).requestCleanup(eq(testingEntity));
    }

    @Test
    void triggerExpirySessions_oneSessionNoUsers() {
        TestingSession testingSession = new TestingSession();
        testingSession.setState(TestingState.ACTIVE);
        when(testingSessionService.getExpiredSessionsByState(any(), eq(TestingState.ACTIVE))).thenReturn(Collections.singletonList(testingSession));
        when(testingSessionService.getExpiredSessionsByState(any(), eq(TestingState.REMOVE_DEPENDENCIES))).thenReturn(Collections.emptyList());

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
        when(testingSessionService.getExpiredSessionsByState(any(), eq(TestingState.ACTIVE))).thenReturn(Collections.emptyList());
        when(testingSessionService.getExpiredSessionsByState(any(), eq(TestingState.REMOVE_DEPENDENCIES))).thenReturn(Collections.singletonList(testingSession));

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
        when(testingSessionService.getExpiredSessionsByState(any(), eq(TestingState.ACTIVE))).thenReturn(Collections.emptyList());
        when(testingSessionService.getExpiredSessionsByState(any(), eq(TestingState.REMOVE_DEPENDENCIES))).thenReturn(Collections.singletonList(testingSession));

        underTest.triggerExpirySessions();

        verify(testingSessionService, never()).requestCleanup(eq(testingSession));
        verify(testingSessionService, never()).updateSession(eq(testingSession));
        verify(testingUserService, never()).requestCleanup(any());
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
    void cleanupSession() {
        CleanupSession cleanupSession = new CleanupSession();
        cleanupSession.setTestingSessionId("test-session-id");
        underTest.cleanupSession(cleanupSession);
        verify(testingSessionService, times(1)).deleteSession(eq("test-session-id"));
    }

    @Test
    void cleanupSessionWithRoles() {
        CleanupSession cleanupSession = new CleanupSession();
        cleanupSession.setTestingSessionId("test-session-id");
        TestingEntity testingEntity = new TestingEntity();
        when(testingRoleService.getTestingEntitiesForSessionById("test-session-id")).thenReturn(Collections.singletonList(testingEntity));
        underTest.cleanupSession(cleanupSession);
        verify(testingSessionService, times(1)).deleteSession(eq("test-session-id"));
        verify(testingRoleService, times(1)).requestCleanup(eq(testingEntity));
        verify(testingServiceProviderService, never()).requestCleanup(any());
    }

    @Test
    void cleanupSessionWithServices() {
        CleanupSession cleanupSession = new CleanupSession();
        cleanupSession.setTestingSessionId("test-session-id");
        TestingEntity testingEntity = new TestingEntity();
        when(testingServiceProviderService.getTestingEntitiesForSessionById("test-session-id")).thenReturn(Collections.singletonList(testingEntity));
        underTest.cleanupSession(cleanupSession);
        verify(testingSessionService, times(1)).deleteSession(eq("test-session-id"));
        verify(testingRoleService, never()).requestCleanup(any());
        verify(testingServiceProviderService, times(1)).requestCleanup(eq(testingEntity));
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
}
