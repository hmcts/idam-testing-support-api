package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingSessionRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestingSessionServiceTest {

    @Mock
    TestingSessionRepo testingSessionRepo;

    @InjectMocks
    TestingSessionService underTest;

    /**
     * @verifies return existing session
     * @see TestingSessionService#getOrCreateSession(String, String)
     */
    @Test
    public void getOrCreateSession_shouldReturnExistingSession() throws Exception {
        TestingSession testingSession = new TestingSession();
        when(testingSessionRepo.findBySessionKey("test-session")).thenReturn(testingSession);
        assertEquals(testingSession, underTest.getOrCreateSession("test-session", "test-client"));
        verify(testingSessionRepo, never()).save(any());
    }

    /**
     * @verifies create new session
     * @see TestingSessionService#getOrCreateSession(String, String)
     */
    @Test
    public void getOrCreateSession_shouldCreateNewSession() throws Exception {
        when(testingSessionRepo.findBySessionKey("test-session")).thenReturn(null);
        when(testingSessionRepo.save(any())).then(returnsFirstArg());
        TestingSession result = underTest.getOrCreateSession("test-session", "test-client");
        assertEquals("test-session", result.getSessionKey());
        assertEquals("test-client", result.getClientId());
        verify(testingSessionRepo, times(1)).save(any());
    }
}
