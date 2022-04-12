package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cft.idam.testingsupportapi.receiver.CleanupReceiver.CLEANUP_USER;

@ExtendWith(MockitoExtension.class)
class TestingEntityServiceTest {

    @Mock
    TestingEntityRepo testingEntityRepo;

    @Mock
    JmsTemplate jmsTemplate;

    TestingEntityService underTest;

    @BeforeEach
    public void initialise() {
        underTest = mock(
            TestingEntityService.class,
            Mockito.withSettings()
                .useConstructor(testingEntityRepo, jmsTemplate)
                .defaultAnswer(Mockito.CALLS_REAL_METHODS));

    }

    @Test
    void requestCleanup() {
        TestingEntity testingEntity = new TestingEntity();
        testingEntity.setEntityId("test-user-id");
        testingEntity.setEntityType(TestingEntityType.USER);

        underTest.requestCleanup(testingEntity);
        verify(testingEntityRepo, times(1)).delete(any());
        verify(jmsTemplate, times(1)).convertAndSend(eq(CLEANUP_USER), eq(testingEntity));
    }
}
