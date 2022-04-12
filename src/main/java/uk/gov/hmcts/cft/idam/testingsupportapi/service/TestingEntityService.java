package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.springframework.jms.core.JmsTemplate;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;

import static uk.gov.hmcts.cft.idam.testingsupportapi.receiver.CleanupReceiver.CLEANUP_USER;

public abstract class TestingEntityService {

    protected final TestingEntityRepo testingEntityRepo;

    private final JmsTemplate jmsTemplate;

    protected TestingEntityService(TestingEntityRepo testingEntityRepo, JmsTemplate jmsTemplate) {
        this.testingEntityRepo = testingEntityRepo;
        this.jmsTemplate = jmsTemplate;
    }

    public void requestCleanup(TestingEntity testingEntity) {
        testingEntityRepo.delete(testingEntity);
        if (testingEntity.getEntityType() == TestingEntityType.USER) {
            jmsTemplate.convertAndSend(CLEANUP_USER, testingEntity);
        }
    }

}
