package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSessionState;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
        testingSession.setState(TestingSessionState.OPEN);
        TestingEntity testingEntity = new TestingEntity();
        when(testingSessionService.getExpiredSessions(any())).thenReturn(Collections.singletonList(testingSession));
        when(testingUserService.getUsersForSession(any())).thenReturn(Collections.singletonList(testingEntity));
        underTest.triggerExpirySessions();

        verify(testingSessionService, times(1)).updateSession(eq(testingSession));
        verify(testingUserService, times(1)).requestCleanup(eq(testingEntity));
    }

    @Test
    void triggerExpirySessions_oneSessionNoUsers() {
        TestingSession testingSession = new TestingSession();
        testingSession.setState(TestingSessionState.OPEN);
        when(testingSessionService.getExpiredSessions(any())).thenReturn(Collections.singletonList(testingSession));
        when(testingUserService.getUsersForSession(any())).thenReturn(Collections.emptyList());
        underTest.triggerExpirySessions();

        verify(testingSessionService, times(1)).deleteSession(eq(testingSession));
        verify(testingSessionService, never()).updateSession(eq(testingSession));
        verify(testingUserService, never()).requestCleanup(any());
    }

    @Test
    void deleteUser() {
        TestingEntity testingEntity = new TestingEntity();
        User user = new User();
        when(testingUserService.deleteIdamUserIfPresent(testingEntity)).thenReturn(Optional.of(user));
        underTest.deleteUser(testingEntity);
        when(testingUserService.deleteIdamUserIfPresent(testingEntity)).thenReturn(Optional.empty());
        underTest.deleteUser(testingEntity);
    }
}
