package uk.gov.hmcts.cft.idam.testingsupportapi.internal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupSession;

@FeignClient(name = "internalAdminApi", url = "localhost:${server.port}")
public interface InternalAdminApi {

    @PostMapping("/trigger/expiry/burner/users")
    void triggerExpiryBurnerUsers();

    @PostMapping("/trigger/expiry/sessions")
    void triggerExpirySessions();

    @DeleteMapping("/admin/entities/users")
    void deleteUserTestingEntity(@RequestBody CleanupEntity entity);

    @DeleteMapping("/admin/sessions")
    void deleteSession(@RequestBody CleanupSession session);

    @DeleteMapping("/admin/entities/roles")
    void deleteRoleTestingEntity(@RequestBody CleanupEntity entity);

    @DeleteMapping("/admin/entities/services")
    void deleteServiceTestingEntity(@RequestBody CleanupEntity entity);

}
