package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingSessionRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingState;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.cft.idam.testingsupportapi.receiver.CleanupReceiver.CLEANUP_SESSION;

@Service
public class TestingSessionService {

    private final TestingSessionRepo testingSessionRepo;

    private final JmsTemplate jmsTemplate;

    public TestingSessionService(TestingSessionRepo testingSessionRepo, JmsTemplate jmsTemplate) {
        this.testingSessionRepo = testingSessionRepo;
        this.jmsTemplate = jmsTemplate;
    }

    /**
     * Get or create session.
     * @should return existing session
     * @should create new session
     */
    public TestingSession getOrCreateSession(String sessionKey, String clientId) {
        TestingSession existingSession = testingSessionRepo.findBySessionKey(sessionKey);
        if (existingSession != null) {
            return existingSession;
        }
        TestingSession newSession = new TestingSession();
        newSession.setId(UUID.randomUUID().toString());
        newSession.setClientId(clientId);
        newSession.setSessionKey(sessionKey);
        newSession.setState(TestingState.ACTIVE);
        newSession.setCreateDate(ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
        return testingSessionRepo.save(newSession);
    }

    /**
     * Update Session.
     * @should update session
     */
    public TestingSession updateSession(TestingSession testingSession) {
        return testingSessionRepo.save(testingSession);
    }

    /**
     * Delete Session.
     * @should delete session
     */
    public void deleteSession(String testingSessionId) {
        testingSessionRepo.deleteById(testingSessionId);
    }

    /**
     * Get expired sessions.
     * @should get expired sessions.
     */
    public List<TestingSession> getExpiredSessions(ZonedDateTime cleanupTime) {
        return
            testingSessionRepo
                .findTop10ByCreateDateBeforeOrderByCreateDateAsc(cleanupTime);
    }

    /**
     * Request cleanup.
     * @should request cleanup
     */
    public void requestCleanup(TestingSession testingSession) {
        testingSession.setState(TestingState.REMOVE);
        testingSession.setLastModifiedDate(ZonedDateTime.now());
        testingSessionRepo.save(testingSession);
        CleanupSession cleanupSession = new CleanupSession();
        cleanupSession.setTestingSessionId(testingSession.getId());
        jmsTemplate.convertAndSend(CLEANUP_SESSION, cleanupSession);
    }

}
