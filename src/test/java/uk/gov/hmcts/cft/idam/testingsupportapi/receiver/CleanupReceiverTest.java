package uk.gov.hmcts.cft.idam.testingsupportapi.receiver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.AdminService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CleanupReceiverTest {

    @Mock
    AdminService adminService;

    @InjectMocks
    CleanupReceiver underTest;

    @Test
    void receiveUser() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        underTest.receiveUser(cleanupEntity);
        verify(adminService, times(1)).cleanupUser(cleanupEntity);
    }

    @Test
    void receiveUserWithException() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        doThrow(SpringWebClientHelper.notFound()).when(adminService).cleanupUser(any());
        try {
            underTest.receiveUser(cleanupEntity);
            fail();
        } catch (HttpStatusCodeException hsce) {
            assertEquals(HttpStatus.NOT_FOUND, hsce.getStatusCode());
        }
    }

    @Test
    void receiveSession() {
        CleanupSession cleanupSession = new CleanupSession();
        underTest.receiveSession(cleanupSession);
        verify(adminService, times(1)).cleanupSession(cleanupSession);
    }

    @Test
    void receiveRole() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        underTest.receiveRole(cleanupEntity);
        verify(adminService, times(1)).cleanupRole(cleanupEntity);
    }

    @Test
    void receiveService() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        underTest.receiveService(cleanupEntity);
        verify(adminService, times(1)).cleanupService(cleanupEntity);
    }

    @Test
    void receiveUserProfile() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        underTest.receiveUserProfile(cleanupEntity);
        verify(adminService, times(1)).cleanupUserProfile(cleanupEntity);
    }

    @Test
    void receiveCaseWorkerProfile() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        underTest.receiveCaseWorkerProfile(cleanupEntity);
        verify(adminService, times(1)).cleanupCaseWorkerProfile(cleanupEntity);
    }
}
