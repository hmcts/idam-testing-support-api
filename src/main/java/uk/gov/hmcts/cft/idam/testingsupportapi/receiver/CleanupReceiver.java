package uk.gov.hmcts.cft.idam.testingsupportapi.receiver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cft.idam.testingsupportapi.internal.InternalAdminApi;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupSession;

@Slf4j
@Component
public class CleanupReceiver {

    public static final String CLEANUP_USER = "cleanup-user";

    public static final String CLEANUP_SESSION = "cleanup-session";

    public static final String CLEANUP_ROLE = "cleanup-role";

    public static final String CLEANUP_SERVICE = "cleanup-service";

    private final InternalAdminApi internalAdminApi;

    public CleanupReceiver(InternalAdminApi internalAdminApi) {
        this.internalAdminApi = internalAdminApi;
    }

    @JmsListener(destination = CLEANUP_USER)
    public void receiveUser(CleanupEntity entity) {
        internalAdminApi.deleteUserTestingEntity(entity);
    }

    @JmsListener(destination = CLEANUP_SESSION)
    public void receiveSession(CleanupSession session) {
        internalAdminApi.deleteSession(session);
    }

    @JmsListener(destination = CLEANUP_ROLE)
    public void receiveRole(CleanupEntity entity) {
        internalAdminApi.deleteRoleTestingEntity(entity);
    }

    @JmsListener(destination = CLEANUP_SERVICE)
    public void receiveService(CleanupEntity entity) {
        internalAdminApi.deleteServiceTestingEntity(entity);
    }


}
