package uk.gov.hmcts.cft.idam.api.v1;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.hmcts.cft.idam.api.v2.common.auth.IdamClientCredentialsConfig;

@FeignClient(name = "idamv0usermanagement", url = "${idam.api.url}", configuration = IdamClientCredentialsConfig.class)
public interface IdamV1UserManagementApi {

    @DeleteMapping("/api/v1/users/{userId}")
    void deleteUserByUserId(@PathVariable String userId);

}
