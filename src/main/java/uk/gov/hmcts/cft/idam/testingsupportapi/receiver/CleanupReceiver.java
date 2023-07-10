package uk.gov.hmcts.cft.idam.testingsupportapi.receiver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.AdminService;

@Slf4j
@Component
public class CleanupReceiver {

    public static final String CLEANUP_USER = "cleanup-user";

    public static final String CLEANUP_SESSION = "cleanup-session";

    public static final String CLEANUP_ROLE = "cleanup-role";

    public static final String CLEANUP_SERVICE = "cleanup-service";
    private final AdminService adminService;

    public CleanupReceiver(AdminService adminService) {
        this.adminService = adminService;
    }

    @JmsListener(destination = CLEANUP_USER)
    public void receiveUser(CleanupEntity entity) {
        log.info("Received cleanup request for entity id {}, for user {}",
                 entity.getTestingEntityId(), entity.getEntityId());
        adminService.cleanupUser(entity);
    }

    @JmsListener(destination = CLEANUP_SESSION)
    public void receiveSession(CleanupSession session) {
        adminService.cleanupSession(session);
    }

    @JmsListener(destination = CLEANUP_ROLE)
    public void receiveRole(CleanupEntity entity) {
        adminService.cleanupRole(entity);
    }

    @JmsListener(destination = CLEANUP_SERVICE)
    public void receiveService(CleanupEntity entity) {
        adminService.cleanupService(entity);
    }


}
