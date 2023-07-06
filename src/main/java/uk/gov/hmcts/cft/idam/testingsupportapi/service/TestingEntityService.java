package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.apache.commons.collections4.CollectionUtils;
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
import static uk.gov.hmcts.cft.idam.testingsupportapi.receiver.CleanupReceiver.CLEANUP_SERVICE;
import static uk.gov.hmcts.cft.idam.testingsupportapi.receiver.CleanupReceiver.CLEANUP_USER;

public abstract class TestingEntityService<T> {

    protected final TestingEntityRepo testingEntityRepo;

    private final JmsTemplate jmsTemplate;

    enum MissingEntityStrategy {
        CREATE, IGNORE
    }

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
        } else if (testingEntity.getEntityType() == TestingEntityType.SERVICE) {
            jmsTemplate.convertAndSend(CLEANUP_SERVICE, cleanupEntity);
        }
    }

    public boolean deleteTestingEntityById(String testingEntityId) {
        if (testingEntityRepo.existsById(testingEntityId)) {
            testingEntityRepo.deleteById(testingEntityId);
            return true;
        }
        return false;
    }

    public List<TestingEntity> getTestingEntitiesForSession(TestingSession testingSession) {
        return getTestingEntitiesForSessionById(testingSession.getId());
    }

    public List<TestingEntity> getTestingEntitiesForSessionById(String sessionId) {
        // TODO state needs to be active
        return testingEntityRepo.findByTestingSessionIdAndEntityTypeAndState(sessionId, getTestingEntityType(), TestingState.ACTIVE);
    }

    public void addTestEntityToSessionForRemoval(TestingSession session, String entityId) {
        removeTestEntity(session.getId(), entityId, MissingEntityStrategy.CREATE);
    }

    protected void removeTestEntity(String sessionId, String entityId, MissingEntityStrategy missingEntityStrategy) {
        List<TestingEntity> testingEntityList = testingEntityRepo
            .findAllByEntityIdAndEntityTypeAndState(entityId, getTestingEntityType(), TestingState.ACTIVE);
        if (CollectionUtils.isNotEmpty(testingEntityList)) {
            testingEntityList.stream().filter(te -> te.getState() == TestingState.ACTIVE).forEach(this::requestCleanup);
        } else if (missingEntityStrategy == MissingEntityStrategy.CREATE) {
            TestingEntity newEntity = buildTestingEntity(sessionId, entityId, getTestingEntityType());
            testingEntityRepo.save(newEntity);
        }
    }

    protected abstract void deleteEntity(String key);

    protected abstract String getEntityKey(T entity);

    protected abstract TestingEntityType getTestingEntityType();

    protected TestingEntity createTestingEntity(String sessionId, T requestEntity) {
        TestingEntity testingEntity =
            buildTestingEntity(sessionId, getEntityKey(requestEntity), getTestingEntityType());
        testingEntity = testingEntityRepo.save(testingEntity);
        return testingEntity;
    }

    protected TestingEntity buildTestingEntity(String sessionId, String entityId, TestingEntityType type) {
        TestingEntity testingEntity = new TestingEntity();
        testingEntity.setId(UUID.randomUUID().toString());
        testingEntity.setEntityId(entityId);
        testingEntity.setEntityType(type);
        testingEntity.setTestingSessionId(sessionId);
        testingEntity.setState(TestingState.ACTIVE);
        testingEntity.setCreateDate(ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));
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
