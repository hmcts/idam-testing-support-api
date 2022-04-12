package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.model.UserTestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TestingUserService extends TestingEntityService {

    private final IdamV0Service idamV0Service;

    public TestingUserService(IdamV0Service idamV0Service, TestingEntityRepo testingEntityRepo,
                              JmsTemplate jmsTemplate) {
        super(testingEntityRepo, jmsTemplate);
        this.idamV0Service = idamV0Service;
    }

    /**
     * Create test user.
     *
     * @should create user and testing entity
     */
    public UserTestingEntity createTestUser(String sessionId, User requestUser, String secretPhrase) {
        User testUser = idamV0Service.createTestUser(requestUser, secretPhrase);

        TestingEntity testingEntity = new TestingEntity();
        testingEntity.setId(UUID.randomUUID().toString());
        testingEntity.setEntityId(testUser.getId());
        testingEntity.setEntityType(TestingEntityType.USER);
        testingEntity.setTestingSessionId(sessionId);
        testingEntity.setCreateDate(ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));

        testingEntity = testingEntityRepo.save(testingEntity);

        UserTestingEntity result = new UserTestingEntity();
        result.setTestingEntity(testingEntity);
        result.setUser(testUser);

        return result;

    }

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
     */
    public Optional<User> deleteIdamUserIfPresent(TestingEntity testingEntity) {

        Optional<User> user = idamV0Service.findUserById(testingEntity.getEntityId());
        if (user.isPresent()) {
            idamV0Service.deleteUser(user.get());
            return user;
        }

        return Optional.empty();
    }

}
