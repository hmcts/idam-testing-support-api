package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.cft.idam.api.v2.IdamV2UserManagementApi;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ActivatedUserRequest;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.model.UserTestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingState;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class TestingUserService extends TestingEntityService {

    private final IdamV2UserManagementApi idamV2UserManagementApi;

    public TestingUserService(IdamV2UserManagementApi idamV2UserManagementApi, TestingEntityRepo testingEntityRepo,
                              JmsTemplate jmsTemplate) {
        super(testingEntityRepo, jmsTemplate);
        this.idamV2UserManagementApi = idamV2UserManagementApi;
    }

    /**
     * Create test user.
     *
     * @should create user and testing entity
     * @should create user and testing entity with roles
     * @should report if created roles do not match request
     */
    public UserTestingEntity createTestUser(String sessionId, User requestUser, String secretPhrase) {
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

        TestingEntity testingEntity = new TestingEntity();
        testingEntity.setId(UUID.randomUUID().toString());
        testingEntity.setEntityId(testUser.getId());
        testingEntity.setEntityType(TestingEntityType.USER);
        testingEntity.setTestingSessionId(sessionId);
        testingEntity.setState(TestingState.ACTIVE);
        testingEntity.setCreateDate(ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));

        testingEntity = testingEntityRepo.save(testingEntity);

        UserTestingEntity result = new UserTestingEntity();
        result.setTestingEntity(testingEntity);
        result.setUser(testUser);

        return result;

    }

    private boolean safeIsEqualCollection(final Collection<?> a, final Collection<?> b) {
        return (a == null && b == null)
            || (a != null && b != null && CollectionUtils.isEqualCollection(a, b));
    }

    /**
     * Get users for session.
     *
     * @should get users for session
     */
    public List<TestingEntity> getUsersForSession(TestingSession testingSession) {
        return testingEntityRepo.findByTestingSessionId(testingSession.getId());
    }

    /**
     * Get expired burner users.
     *
     * @should get expired burner users
     */
    public List<TestingEntity> getExpiredBurnerUserTestingEntities(ZonedDateTime cleanupTime) {
        return testingEntityRepo.findTop10ByEntityTypeAndCreateDateBeforeAndTestingSessionIdIsNullOrderByCreateDateAsc(
            TestingEntityType.USER,
            cleanupTime
        );

    }

    /**
     * Delete user if present.
     *
     * @should delete user and testing entity if present
     * @should return empty if no user
     * @should throw exception for other errors
     */
    public Optional<User> deleteIdamUserIfPresent(String userId) {
        try {
            return Optional.of(idamV2UserManagementApi.deleteUser(userId));
        } catch (HttpClientErrorException hcee) {
            if (hcee.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw hcee;
        }
    }

}
