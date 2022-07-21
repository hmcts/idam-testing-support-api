package uk.gov.hmcts.cft.idam.testingsupportapi.receiver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cft.idam.testingsupportapi.internal.InternalAdminApi;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupSession;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CleanupReceiverTest {

    @Mock
    InternalAdminApi internalAdminApi;

    @InjectMocks
    CleanupReceiver underTest;

    @Test
    void receiveUser() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        underTest.receiveUser(cleanupEntity);
        verify(internalAdminApi, times(1)).deleteUserTestingEntity(eq(cleanupEntity));
    }

    @Test
    void receiveSession() {
        CleanupSession cleanupSession = new CleanupSession();
        underTest.receiveSession(cleanupSession);
        verify(internalAdminApi, times(1)).deleteSession(cleanupSession);
    }

    @Test
    void receiveRole() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        underTest.receiveRole(cleanupEntity);
        verify(internalAdminApi, times(1)).deleteRoleTestingEntity(eq(cleanupEntity));
    }
}
