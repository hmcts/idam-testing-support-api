package uk.gov.hmcts.cft.idam.testingsupportapi.receiver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cft.idam.testingsupportapi.internal.InternalAdminApi;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;

@Slf4j
@Component
public class CleanupReceiver {

    public static final String CLEANUP_USER = "cleanup-user";

    private final InternalAdminApi internalAdminApi;

    public CleanupReceiver(InternalAdminApi internalAdminApi) {
        this.internalAdminApi = internalAdminApi;
    }

    @JmsListener(destination = CLEANUP_USER)
    public void receiveUser(TestingEntity entity) {
        internalAdminApi.deleteUserTestingEntity(entity);
    }

}
