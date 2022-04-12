package uk.gov.hmcts.cft.idam.testingsupportapi.internal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;

@FeignClient(name = "internalAdminApi", url = "localhost:${server.port}")
public interface InternalAdminApi {

    @PostMapping("/trigger/expiry/burner/users")
    void triggerExpiryBurnerUsers();

    @PostMapping("/trigger/expiry/sessions")
    void triggerExpirySessions();

    @DeleteMapping("/admin/entities/users")
    void deleteUserTestingEntity(@RequestBody TestingEntity testingEntity);
}
