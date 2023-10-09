package uk.gov.hmcts.cft.idam.testingsupportapi.receiver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.AdminService;

import static org.mockito.ArgumentMatchers.eq;
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
        verify(adminService, times(1)).cleanupUser(eq(cleanupEntity));
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
        verify(adminService, times(1)).cleanupRole(eq(cleanupEntity));
    }

    @Test
    void receiveService() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        underTest.receiveService(cleanupEntity);
        verify(adminService, times(1)).cleanupService(eq(cleanupEntity));
    }

    @Test
    void receiveUserProfile() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        underTest.receiveUserProfile(cleanupEntity);
        verify(adminService, times(1)).cleanupUserProfile(eq(cleanupEntity));
    }
}
