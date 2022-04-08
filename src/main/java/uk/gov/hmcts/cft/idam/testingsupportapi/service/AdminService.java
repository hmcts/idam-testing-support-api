package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AdminService {

    private final TestingUserService testingUserService;

    private final JmsTemplate jmsTemplate;

    public AdminService(TestingUserService testingUserService, JmsTemplate jmsTemplate) {
        this.testingUserService = testingUserService;
        this.jmsTemplate = jmsTemplate;
    }

    public void checkExpiry() {

        // check for burner users to remove
        List<TestingEntity> burnerEntities = testingUserService.getExpiredBurnerUserTestingEntities();
        if (CollectionUtils.isNotEmpty(burnerEntities)) {
            log.info("Found {} burner user(s)", burnerEntities.size());
            for (TestingEntity burnerEntity : burnerEntities) {
                testingUserService.deleteTestingEntity(burnerEntity);
                jmsTemplate.convertAndSend("cleanup", burnerEntity);
            }
        } else {
            log.info("No burner users to remove");
        }

        // check for sessions that have expired

    }

    public void deleteUser(TestingEntity testingEntity) {
        Optional<User> user = testingUserService.deleteUserIfPresent(testingEntity);
        if (user.isPresent()) {
            log.info("Deleted user {}", testingEntity.getEntityId());
        } else {
            log.info("No user found for id {}", testingEntity.getEntityId());
        }
    }

}
