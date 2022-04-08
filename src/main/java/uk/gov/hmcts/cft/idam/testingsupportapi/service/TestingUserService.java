package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.model.UserTestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TestingUserService {

    @Value("${cleanup.burner.lifespan}")
    private Duration burnerLifespan;

    private final IdamV0Service idamV0Service;

    private final TestingEntityRepo testingEntityRepo;

    public TestingUserService(IdamV0Service idamV0Service, TestingEntityRepo testingEntityRepo) {
        this.idamV0Service = idamV0Service;
        this.testingEntityRepo = testingEntityRepo;
    }

    /**
     * Create test user.
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

    public List<TestingEntity> getExpiredBurnerUserTestingEntities() {

        ZonedDateTime cleanupTime = ZonedDateTime.now().minus(burnerLifespan);

        return
            testingEntityRepo
                .findTop10ByEntityTypeAndCreateDateBeforeAndTestingSessionIdIsNullOrderByCreateDateAsc(
                    TestingEntityType.USER, cleanupTime);

    }

    public void deleteTestingEntity(TestingEntity testingEntity) {
        testingEntityRepo.delete(testingEntity);
    }

    public Optional<User> deleteUserIfPresent(TestingEntity testingEntity) {

        Optional<User> user = idamV0Service.findUserById(testingEntity.getEntityId());
        if (user.isPresent()) {
            idamV0Service.deleteUser(user.get());
            return user;
        }

        return Optional.empty();
    }

}
