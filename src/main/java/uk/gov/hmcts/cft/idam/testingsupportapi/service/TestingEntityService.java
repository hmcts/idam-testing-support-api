package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingState;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.cft.idam.testingsupportapi.receiver.CleanupReceiver.CLEANUP_ROLE;
import static uk.gov.hmcts.cft.idam.testingsupportapi.receiver.CleanupReceiver.CLEANUP_USER;

public abstract class TestingEntityService<T> {

    protected final TestingEntityRepo testingEntityRepo;

    private final JmsTemplate jmsTemplate;

    protected TestingEntityService(TestingEntityRepo testingEntityRepo, JmsTemplate jmsTemplate) {
        this.testingEntityRepo = testingEntityRepo;
        this.jmsTemplate = jmsTemplate;
    }

    public void requestCleanup(TestingEntity testingEntity) {
        CleanupEntity cleanupEntity = new CleanupEntity();
        cleanupEntity.setTestingEntityId(testingEntity.getId());
        cleanupEntity.setEntityId(testingEntity.getEntityId());
        cleanupEntity.setTestingEntityType(testingEntity.getEntityType());
        if (testingEntity.getEntityType() == TestingEntityType.USER) {
            jmsTemplate.convertAndSend(CLEANUP_USER, cleanupEntity);
        } else if (testingEntity.getEntityType() == TestingEntityType.ROLE) {
            jmsTemplate.convertAndSend(CLEANUP_ROLE, cleanupEntity);
        }
    }

    public void deleteTestingEntityById(String entityId) {
        testingEntityRepo.deleteById(entityId);
    }

    public List<TestingEntity> getTestingEntitiesForSession(TestingSession testingSession) {
        return getTestingEntitiesForSessionById(testingSession.getId());
    }

    public List<TestingEntity> getTestingEntitiesForSessionById(String sessionId) {
        return testingEntityRepo.findByTestingSessionIdAndEntityType(sessionId, getTestingEntityType());
    }

    protected abstract void deleteEntity(String key);

    protected abstract String getEntityKey(T entity);

    protected abstract TestingEntityType getTestingEntityType();

    protected TestingEntity createTestingEntity(String sessionKey, T requestEntity) {

        TestingEntity testingEntity = new TestingEntity();
        testingEntity.setId(UUID.randomUUID().toString());
        testingEntity.setEntityId(getEntityKey(requestEntity));
        testingEntity.setEntityType(getTestingEntityType());
        testingEntity.setTestingSessionId(sessionKey);
        testingEntity.setState(TestingState.ACTIVE);
        testingEntity.setCreateDate(ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));

        testingEntity = testingEntityRepo.save(testingEntity);

        return testingEntity;
    }

    public boolean delete(String key) {
        try {
            deleteEntity(key);
            return true;
        } catch (HttpClientErrorException hcee) {
            if (hcee.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw hcee;
        }
    }

}
