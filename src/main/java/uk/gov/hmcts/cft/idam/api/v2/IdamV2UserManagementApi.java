package uk.gov.hmcts.cft.idam.api.v2;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.cft.idam.api.v2.common.auth.IdamClientCredentialsConfig;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ActivatedUserRequest;
import uk.gov.hmcts.cft.idam.api.v2.common.model.Role;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;

import javax.validation.Valid;

@FeignClient(name = "idamv2usermanagement", url = "${idam.api.url}", configuration = IdamClientCredentialsConfig.class)
public interface IdamV2UserManagementApi {

    @PostMapping("/api/v2/users")
    User createUser(@Valid @RequestBody ActivatedUserRequest activatedUserRequest);

    @GetMapping("/api/v2/users/{userId}")
    User getUser(@PathVariable String userId);

    @DeleteMapping("/api/v2/users/{userId}")
    void deleteUser(@PathVariable String userId);

}
