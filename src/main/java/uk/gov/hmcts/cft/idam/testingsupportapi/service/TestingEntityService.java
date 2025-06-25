package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import io.opentelemetry.api.trace.Span;
import jakarta.transaction.Transactional;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingState;
import uk.gov.hmcts.cft.idam.testingsupportapi.trace.TraceAttribute;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.cft.idam.testingsupportapi.receiver.CleanupReceiver.CLEANUP_CASEWORKER;
import static uk.gov.hmcts.cft.idam.testingsupportapi.receiver.CleanupReceiver.CLEANUP_INVITATION;
import static uk.gov.hmcts.cft.idam.testingsupportapi.receiver.CleanupReceiver.CLEANUP_PROFILE;
import static uk.gov.hmcts.cft.idam.testingsupportapi.receiver.CleanupReceiver.CLEANUP_ROLE;
import static uk.gov.hmcts.cft.idam.testingsupportapi.receiver.CleanupReceiver.CLEANUP_SERVICE;
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
        cleanupEntity.setTestingSessionId(testingEntity.getTestingSessionId());
        String cleanupDestination = getCleanupDestination(testingEntity.getEntityType());
        if (cleanupDestination!= null) {
            jmsTemplate.convertAndSend(cleanupDestination, cleanupEntity);
        }
    }

    protected String getCleanupDestination(TestingEntityType testingEntityType) {
        if (testingEntityType == TestingEntityType.USER) {
            return CLEANUP_USER;
        } else if (testingEntityType == TestingEntityType.ROLE) {
            return CLEANUP_ROLE;
        } else if (testingEntityType == TestingEntityType.SERVICE) {
            return CLEANUP_SERVICE;
        } else if (testingEntityType == TestingEntityType.PROFILE) {
            return CLEANUP_PROFILE;
        } else if (testingEntityType == TestingEntityType.PROFILE_CASEWORKER) {
            return CLEANUP_CASEWORKER;
        } else if (testingEntityType == TestingEntityType.INVITATION) {
            return CLEANUP_INVITATION;
        }
        return null;
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
        return testingEntityRepo.findByTestingSessionIdAndEntityTypeAndState(sessionId,
                                                                             getTestingEntityType(),
                                                                             TestingState.ACTIVE
        );
    }

    public void addTestEntityToSessionForRemoval(TestingSession session, String entityId) {
        removeTestEntity(session.getId(), entityId, MissingEntityStrategy.CREATE);
    }

    protected void removeTestEntity(String sessionId, String entityId, MissingEntityStrategy missingEntityStrategy) {
        List<TestingEntity> testingEntityList = findAllActiveByEntityId(entityId);
        if (CollectionUtils.isNotEmpty(testingEntityList)) {
            testingEntityList.stream().filter(te -> te.getState() == TestingState.ACTIVE).forEach(this::requestCleanup);
        } else if (missingEntityStrategy == MissingEntityStrategy.CREATE) {
            TestingEntity newEntity = buildTestingEntity(sessionId, entityId, getTestingEntityType());
            testingEntityRepo.save(newEntity);
        }
    }

    public List<TestingEntity> findAllActiveByEntityId(String entityId) {
        return testingEntityRepo.findAllByEntityIdAndEntityTypeAndState(entityId,
                                                                        getTestingEntityType(),
                                                                        TestingState.ACTIVE
        );
    }

    public enum CleanupFailureStrategy {
        FAIL, DETACH, CUSTOM;
    }

    protected abstract void deleteEntity(String key);

    protected abstract String getEntityKey(T entity);

    protected abstract TestingEntityType getTestingEntityType();

    protected CleanupFailureStrategy getCleanupFailureStrategy() {
        return CleanupFailureStrategy.FAIL;
    }

    protected TestingEntity createTestingEntity(String sessionId, T requestEntity) {
        TestingEntity testingEntity = buildTestingEntity(sessionId,
                                                         getEntityKey(requestEntity),
                                                         getTestingEntityType()
        );
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

    @Transactional
    public void detachEntity(String testingEntityId) {
        testingEntityRepo.updateTestingStateById(testingEntityId, TestingState.DETACHED);
    }

    enum MissingEntityStrategy {
        CREATE, IGNORE
    }

    public void doCleanup(CleanupEntity cleanupEntity) {
        doCleanup(cleanupEntity, getCleanupFailureStrategy());
    }

    public void doCleanup(CleanupEntity cleanupEntity, CleanupFailureStrategy cleanupFailureStrategy) {
        try {
            if (delete(cleanupEntity.getEntityId())) {
                Span.current().setAttribute(TraceAttribute.OUTCOME, AdminService.DELETED);
            } else {
                Span.current().setAttribute(TraceAttribute.OUTCOME, AdminService.NOT_FOUND);
            }
        } catch (HttpStatusCodeException hsce) {
            if (!handleCleanupException(hsce, cleanupFailureStrategy, cleanupEntity)) {
                throw hsce;
            }
        }
        deleteTestingEntityById(cleanupEntity.getTestingEntityId());
    }

    protected boolean handleCleanupException(Exception e, CleanupFailureStrategy cleanupFailureStrategy, CleanupEntity cleanupEntity) {
        if (cleanupFailureStrategy == CleanupFailureStrategy.DETACH) {
            Span.current().setAttribute(TraceAttribute.OUTCOME, "detached");
            detachEntity(cleanupEntity.getTestingEntityId());
            return true;
        } else {
            return false;
        }
    }

}
