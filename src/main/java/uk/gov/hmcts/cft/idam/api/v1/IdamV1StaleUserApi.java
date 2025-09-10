package uk.gov.hmcts.cft.idam.api.v1;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.cft.idam.api.v1.common.model.V1UserWithRoleIds;

@FeignClient(name = "idamv1staleuser", url = "${idam.api.url}")
public interface IdamV1StaleUserApi {

    /**
     * Note this is a v1 call using client credentials
     */
    @PostMapping("/api/v1/staleUsers/{userId}")
    void createArchivedUser(@PathVariable String userId, @RequestBody V1UserWithRoleIds v1User);

    @DeleteMapping("/api/v1/staleUsers/{userId}")
    void deleteArchivedUser(@PathVariable String userId);
}
