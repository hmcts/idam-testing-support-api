package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import uk.gov.hmcts.cft.idam.api.v1.IdamV1StaleUserApi;
import uk.gov.hmcts.cft.idam.api.v1.common.util.UserConversionUtil;
import uk.gov.hmcts.cft.idam.api.v2.IdamV2UserManagementApi;
import uk.gov.hmcts.cft.idam.api.v2.common.model.AccountStatus;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ActivatedUserRequest;
import uk.gov.hmcts.cft.idam.api.v2.common.model.RecordType;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingState;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class TestingUserService extends TestingEntityService<User> {

    private final IdamV2UserManagementApi idamV2UserManagementApi;
    private final IdamV1StaleUserApi idamV1StaleUserApi;

    @Value("${cleanup.burner.batch-size:10}")
    private int expiredBurnerUserBatchSize;

    @Value("${cleanup.user.strategy}")
    private UserCleanupStrategy userCleanupStrategy;

    @Value("${cleanup.user.recent-login-duration}")
    private Duration recentLoginDuration;

    @Value("${cleanup.session.lifespan}")
    private Duration sessionLifespan;

    private Clock clock;

    public TestingUserService(IdamV2UserManagementApi idamV2UserManagementApi,
                              TestingEntityRepo testingEntityRepo,
                              JmsTemplate jmsTemplate, IdamV1StaleUserApi idamV1StaleUserApi) {
        super(testingEntityRepo, jmsTemplate);
        this.idamV2UserManagementApi = idamV2UserManagementApi;
        this.idamV1StaleUserApi = idamV1StaleUserApi;
        this.clock = Clock.system(ZoneOffset.UTC);
    }

    @PostConstruct
    public void validateProperties() {
        if (recentLoginDuration.compareTo(sessionLifespan) >= 0) {
            log.warn("cleanup.user.recentLoginDuration must be less than cleanup.sessions.lifespan");
            recentLoginDuration = sessionLifespan.dividedBy(2);
            log.warn("recentLoginDuration overridden to {}", recentLoginDuration);
        }
    }

    @VisibleForTesting
    protected void changeClock(Clock clock) {
        this.clock = clock;
    }

    /**
     * Create test user.
     *
     * @should create user and testing entity
     * @should create user and testing entity with roles
     * @should report if created roles do not match request
     */
    public User createTestUser(String sessionId, User requestUser, String secretPhrase) {
        User testUser;
        if (requestUser.getRecordType() == RecordType.ARCHIVED) {
            testUser = createArchivedUser(requestUser);
        } else {
            testUser = createActiveUser(requestUser, secretPhrase);
        }
        createTestingEntity(sessionId, testUser);
        return testUser;
    }

    private User createActiveUser(User requestUser, String secretPhrase) {
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

        return testUser;
    }

    private User createArchivedUser(User requestUser) {
        if (StringUtils.isEmpty(requestUser.getId())) {
            requestUser.setId(UUID.randomUUID().toString());
        }
        idamV1StaleUserApi.createArchivedUser(requestUser.getId(),
                                              UserConversionUtil.convert(requestUser,
                                                                         getRoleIds(requestUser.getRoleNames())));
        return getUserByUserId(requestUser.getId());
    }

    private List<String> getRoleIds(List<String> roleNames) {
        // For test users assume that role names and ids are always the same.
        return roleNames;
    }

    /**
     * @should update user and create testing entity
     */
    public User updateTestUser(String sessionId, User user, String password) {
        if (user.getAccountStatus() == null) {
            user.setAccountStatus(AccountStatus.ACTIVE);
        }
        User testUser = idamV2UserManagementApi.updateUser(user.getId(), user);
        idamV2UserManagementApi.updateUserSecret(user.getId(), password);
        if (CollectionUtils.isEmpty(testingEntityRepo.findAllByEntityIdAndEntityTypeAndState(user.getId(),
                                                                                             getTestingEntityType(),
                                                                                             TestingState.ACTIVE
        ))) {
            createTestingEntity(sessionId, testUser);
        }
        return testUser;
    }

    /**
     * Get user by user id.
     *
     * @should return user
     */
    public User getUserByUserId(String userId) {
        return idamV2UserManagementApi.getUser(userId);
    }

    public User getUserByEmail(String email) {
        return idamV2UserManagementApi.getUserByEmail(email);
    }

    /**
     * Add test user to session or cleanup.
     *
     * @should request cleanup of existing test entity
     * @should add new test entity to session
     * @should ignore non-active test entities
     */
    public void addTestUserToSessionForRemoval(TestingSession session, String userId) {
        addTestEntityToSessionForRemoval(session, userId);
    }

    /**
     * Force remove test user.
     *
     * @should remove entity before cleanup
     */
    public void forceRemoveTestUser(String userId) {
        deleteEntity(userId);
        removeTestEntity(null, userId, MissingEntityStrategy.IGNORE);
    }

    /**
     * Remove test user.
     *
     * @should request cleanup of existing test entity
     * @should create new burner test entity if not already present
     */
    public void removeTestUser(String userId) {
        removeTestEntity(null, userId, MissingEntityStrategy.CREATE);
    }

    private boolean safeIsEqualCollection(final Collection<?> a, final Collection<?> b) {
        return (a == null && b == null) || (a != null && b != null && CollectionUtils.isEqualCollection(a, b));
    }

    /**
     * Get expired burner users.
     *
     * @should get expired burner users
     */
    public List<TestingEntity> getExpiredBurnerUserTestingEntities(ZonedDateTime cleanupTime) {
        PageRequest pageRequest = PageRequest.of(0, expiredBurnerUserBatchSize);
        return testingEntityRepo
            .findByEntityTypeAndCreateDateBeforeAndTestingSessionIdIsNullOrderByCreateDateAsc(TestingEntityType.USER,
                                                                                              cleanupTime,
                                                                                              pageRequest
            )
            .getContent();

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

    public UserCleanupStrategy getUserCleanupStrategy() {
        return userCleanupStrategy;
    }

    public boolean isRecentLogin(String userId) {
        try {
            User user = getUserByUserId(userId);
            if (user.getLastLoginDate() != null && user
                .getLastLoginDate()
                .isAfter(ZonedDateTime.now(clock).minus(recentLoginDuration))) {
                return true;
            }
        } catch (HttpStatusCodeException hsce) {
            if (hsce.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw hsce;
        }
        return false;
    }

    public enum UserCleanupStrategy {
        ALWAYS_DELETE, SKIP_RECENT_LOGINS
    }

}
