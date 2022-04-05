package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.model.UserTestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class TestingUserService {

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
    public UserTestingEntity createTestUser(UUID sessionId, User requestUser, String secretPhrase) {
        User testUser = idamV0Service.createTestUser(requestUser, secretPhrase);

        TestingEntity testingEntity = new TestingEntity();
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

}
