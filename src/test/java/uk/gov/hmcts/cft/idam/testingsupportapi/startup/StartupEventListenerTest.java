package uk.gov.hmcts.cft.idam.testingsupportapi.startup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingSessionRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingState;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StartupEventListenerTest {


    @Mock
    TestingSessionRepo testingSessionRepo;

    @InjectMocks
    StartupEventListener underTest;

    @Test
    void testResetSessionStateOnStartup() {
        underTest.resetSessionStateOnStartup(null);
        verify(testingSessionRepo, times(1)).updateAllSessionStates(TestingState.ACTIVE);
    }

}
