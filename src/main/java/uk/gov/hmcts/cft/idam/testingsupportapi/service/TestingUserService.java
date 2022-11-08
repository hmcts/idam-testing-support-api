package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cft.idam.api.v2.IdamV2UserManagementApi;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ActivatedUserRequest;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingState;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class TestingUserService extends TestingEntityService<User> {

    private final IdamV2UserManagementApi idamV2UserManagementApi;

    @Value("${cleanup.session.batch-size:10}")
    private int expiredBurnerUserBatchSize;

    public TestingUserService(IdamV2UserManagementApi idamV2UserManagementApi, TestingEntityRepo testingEntityRepo,
                              JmsTemplate jmsTemplate) {
        super(testingEntityRepo, jmsTemplate);
        this.idamV2UserManagementApi = idamV2UserManagementApi;
    }

    private enum MISSING_ENTITY_STRATEGY {
        CREATE, IGNORE
    }

    /**
     * Create test user.
     *
     * @should create user and testing entity
     * @should create user and testing entity with roles
     * @should report if created roles do not match request
     */
    public User createTestUser(String sessionId, User requestUser, String secretPhrase) {
        ActivatedUserRequest activatedUserRequest = new ActivatedUserRequest();
        activatedUserRequest.setPassword(secretPhrase);
        activatedUserRequest.setUser(requestUser);
        User testUser = idamV2UserManagementApi.createUser(activatedUserRequest);

        if (!safeIsEqualCollection(requestUser.getRoleNames(), testUser.getRoleNames())) {
            log.info("User {} created with different roles than requested. Requested names: {}, Actual names: {}",
                     testUser.getId(),
                     requestUser.getRoleNames(),
                     testUser.getRoleNames()
            );
        }

        createTestingEntity(sessionId, testUser);

        return testUser;

    }

    /**
     * @should request cleanup of existing test entity
     * @should add new test entity to session
     * @should ignore non-active test entities
     */
    public void addTestUserToSessionForRemoval(TestingSession session, String userId) {
        removeTestUser(session.getSessionKey(), userId, MISSING_ENTITY_STRATEGY.CREATE);
    }

    /**
     * @should remove entity before cleanup
     */
    public void forceRemoveTestUser(String userId) {
        deleteEntity(userId);
        removeTestUser(null, userId, MISSING_ENTITY_STRATEGY.IGNORE);
    }

    /**
     * @should request cleanup of existing test entity
     * @should create new burner test entity if not already present
     */
    public void removeTestUser(String userId) {
        removeTestUser(null, userId, MISSING_ENTITY_STRATEGY.CREATE);
    }

    private void removeTestUser(String sessionKey, String userId, MISSING_ENTITY_STRATEGY missingEntityStrategy) {
        List<TestingEntity> testingEntityList = testingEntityRepo.findAllByEntityIdAndEntityType(userId, TestingEntityType.USER);
        if (CollectionUtils.isNotEmpty(testingEntityList)) {
            testingEntityList.stream().filter(te -> te.getState() == TestingState.ACTIVE).forEach(this::requestCleanup);
        } else if (missingEntityStrategy == MISSING_ENTITY_STRATEGY.CREATE) {
            TestingEntity newEntity = buildTestingEntity(sessionKey, userId, getTestingEntityType());
            testingEntityRepo.save(newEntity);
        }
    }

    private boolean safeIsEqualCollection(final Collection<?> a, final Collection<?> b) {
        return (a == null && b == null)
            || (a != null && b != null && CollectionUtils.isEqualCollection(a, b));
    }

    /**
     * Get expired burner users.
     *
     * @should get expired burner users
     */
    public List<TestingEntity> getExpiredBurnerUserTestingEntities(ZonedDateTime cleanupTime) {
        return testingEntityRepo.findByEntityTypeAndCreateDateBeforeAndTestingSessionIdIsNullOrderByCreateDateAsc(
            TestingEntityType.USER,
            cleanupTime, PageRequest.of(0, expiredBurnerUserBatchSize)
        ).getContent();

    }

    /**
     * @should delete user
     */
    @Override
    protected void deleteEntity(String key) {
        idamV2UserManagementApi.deleteUser(key);
    }

    /**
     * @should get entity key
     */
    @Override
    protected String getEntityKey(User entity) {
        return entity.getId();
    }

    /**
     * @should get entity type
     */
    @Override
    protected TestingEntityType getTestingEntityType() {
        return TestingEntityType.USER;
    }
}
