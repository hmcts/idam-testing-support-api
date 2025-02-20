package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingSessionRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingState;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cft.idam.testingsupportapi.receiver.CleanupReceiver.CLEANUP_SESSION;

@ExtendWith(MockitoExtension.class)
class TestingSessionServiceTest {

    @Mock
    TestingSessionRepo testingSessionRepo;

    @Mock
    JmsTemplate jmsTemplate;

    @InjectMocks
    TestingSessionService underTest;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(underTest, "expiredSessionBatchSize", 10);
    }

    /**
     * @verifies return existing session
     * @see TestingSessionService#getOrCreateSession(String, String)
     */
    @Test
    void getOrCreateSession_shouldReturnExistingSession() throws Exception {
        TestingSession testingSession = new TestingSession();
        when(testingSessionRepo.findFirstBySessionKeyOrderByCreateDateAsc("test-session")).thenReturn(testingSession);
        assertEquals(testingSession, underTest.getOrCreateSession("test-session", "test-client"));
        verify(testingSessionRepo, never()).save(any());
    }

    /**
     * @verifies create new session
     * @see TestingSessionService#getOrCreateSession(String, String)
     */
    @Test
    void getOrCreateSession_shouldCreateNewSession() throws Exception {
        when(testingSessionRepo.findFirstBySessionKeyOrderByCreateDateAsc("test-session")).thenReturn(null);
        when(testingSessionRepo.save(any())).then(returnsFirstArg());
        TestingSession result = underTest.getOrCreateSession("test-session", "test-client");
        assertEquals("test-session", result.getSessionKey());
        assertEquals("test-client", result.getClientId());
        verify(testingSessionRepo, times(1)).save(any());
    }

    /**
     * @verifies get expired sessions by state.
     * @see TestingSessionService#getExpiredSessionsByState(ZonedDateTime, TestingState)
     */
    @Test
    void getExpiredSessionsByState_shouldGetExpiredSessionsByState() throws Exception {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        TestingSession testingSession = new TestingSession();
        Page<TestingSession> testPage = new PageImpl<>(Collections.singletonList(testingSession));
        when(testingSessionRepo.findByCreateDateBeforeAndStateOrderByCreateDateAsc(any(), any(), any()))
        .thenReturn(testPage);
        List<TestingSession> result = underTest.getExpiredSessionsByState(zonedDateTime, TestingState.ACTIVE);
        assertEquals(testingSession, result.get(0));
    }

    /**
     * @verifies update session
     * @see TestingSessionService#updateSession(TestingSession)
     */
    @Test
    void updateSession_shouldUpdateSession() throws Exception {
        TestingSession testingSession = new TestingSession();
        underTest.updateSession(testingSession);
        verify(testingSessionRepo, times(1)).save(eq(testingSession));
    }

    /**
     * @verifies delete session
     * @see TestingSessionService#deleteSession(String)
     */
    @Test
    void deleteSession_shouldDeleteSession() throws Exception {
        underTest.deleteSession("test-session-id");
        verify(testingSessionRepo, times(1)).deleteById("test-session-id");
    }

    /**
     * @verifies request cleanup
     * @see TestingSessionService#requestCleanup(TestingSession)
     */
    @Test
    void requestCleanup_shouldRequestCleanup() throws Exception {
        TestingSession testingSession = new TestingSession();
        underTest.requestCleanup(testingSession);
        verify(testingSessionRepo, times(1)).save(eq(testingSession));
        verify(jmsTemplate, times(1)).convertAndSend(eq(CLEANUP_SESSION), any(CleanupSession.class));
    }

    /**
     * @verifies return existing session for principal
     * @see TestingSessionService#getOrCreateSession(org.springframework.security.oauth2.jwt.Jwt)
     */
    @Test
    void getOrCreateSession_shouldReturnExistingSessionForPrincipal() throws Exception {
        Jwt principal = mock(Jwt.class);
        when(principal.hasClaim("auditTrackingId")).thenReturn(true);
        when(principal.getClaimAsString("auditTrackingId")).thenReturn("test-session");
        when(principal.getClaimAsStringList("aud")).thenReturn(Collections.singletonList("test-client"));
        TestingSession testingSession = new TestingSession();
        when(testingSessionRepo.findFirstBySessionKeyOrderByCreateDateAsc("test-session")).thenReturn(testingSession);
        assertEquals(testingSession, underTest.getOrCreateSession(principal));
        verify(testingSessionRepo, never()).save(any());
    }

    /**
     * @verifies create new session for principal
     * @see TestingSessionService#getOrCreateSession(org.springframework.security.oauth2.jwt.Jwt)
     */
    @Test
    void getOrCreateSession_shouldCreateNewSessionForPrincipal() throws Exception {
        Jwt principal = mock(Jwt.class);
        when(principal.hasClaim("auditTrackingId")).thenReturn(true);
        when(principal.getClaimAsString("auditTrackingId")).thenReturn("test-session");
        when(principal.getClaimAsStringList("aud")).thenReturn(Collections.singletonList("test-client"));
        when(testingSessionRepo.findFirstBySessionKeyOrderByCreateDateAsc("test-session")).thenReturn(null);
        when(testingSessionRepo.save(any())).then(returnsFirstArg());
        TestingSession result = underTest.getOrCreateSession(principal);
        assertEquals("test-session", result.getSessionKey());
        assertEquals("test-client", result.getClientId());
        verify(testingSessionRepo, times(1)).save(any());
    }

}
