package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingSessionRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSessionState;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TestingSessionService {

    private final TestingSessionRepo testingSessionRepo;

    public TestingSessionService(TestingSessionRepo testingSessionRepo) {
        this.testingSessionRepo = testingSessionRepo;
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
        newSession.setState(TestingSessionState.OPEN);
        newSession.setCreateDate(ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
        return testingSessionRepo.save(newSession);
    }

    public TestingSession updateSession(TestingSession testingSession) {
        return testingSessionRepo.save(testingSession);
    }

    public void deleteSession(TestingSession testingSession) {
        testingSessionRepo.delete(testingSession);
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

}
