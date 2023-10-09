package uk.gov.hmcts.cft.idam.testingsupportapi.receiver;

import io.opentelemetry.api.trace.Span;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.AdminService;
import uk.gov.hmcts.cft.idam.testingsupportapi.trace.TraceAttribute;

@Slf4j
@Component
public class CleanupReceiver {

    public static final String CLEANUP_USER = "cleanup-user";

    public static final String CLEANUP_SESSION = "cleanup-session";

    public static final String CLEANUP_ROLE = "cleanup-role";

    public static final String CLEANUP_SERVICE = "cleanup-service";

    public static final String CLEANUP_PROFILE = "cleanup-profile";
    private final AdminService adminService;

    public CleanupReceiver(AdminService adminService) {
        this.adminService = adminService;
    }

    @JmsListener(destination = CLEANUP_USER)
    public void receiveUser(CleanupEntity entity) {
        Span.current()
            .setAttribute(TraceAttribute.USER_ID, entity.getEntityId());
        adminService.cleanupUser(entity);
    }

    @JmsListener(destination = CLEANUP_SESSION)
    public void receiveSession(CleanupSession session) {
        Span.current()
            .setAttribute(TraceAttribute.SESSION_ID, session.getTestingSessionId());
        adminService.cleanupSession(session);
    }

    @JmsListener(destination = CLEANUP_ROLE)
    public void receiveRole(CleanupEntity entity) {
        Span.current()
            .setAttribute(TraceAttribute.ROLE_NAME, entity.getEntityId());
        adminService.cleanupRole(entity);
    }

    @JmsListener(destination = CLEANUP_SERVICE)
    public void receiveService(CleanupEntity entity) {
        Span.current()
            .setAttribute(TraceAttribute.CLIENT_ID, entity.getClientId());
        adminService.cleanupService(entity);
    }

    @JmsListener(destination = CLEANUP_PROFILE)
    public void receiveUserProfile(CleanupEntity entity) {
        Span.current()
            .setAttribute(TraceAttribute.USER_ID, entity.getEntityId());
        adminService.cleanupUserProfile(entity);
    }

}
