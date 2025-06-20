package uk.gov.hmcts.cft.idam.api.v2;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ActivatedUserRequest;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;

@FeignClient(name = "idamv2usermanagement", url = "${idam.api.url}")
public interface IdamV2UserManagementApi {

    @PostMapping("/api/v2/users")
    User createUser(@Valid @RequestBody ActivatedUserRequest activatedUserRequest);

    @GetMapping("/api/v2/users/{userId}")
    User getUser(@PathVariable String userId);

    @GetMapping("/api/v2/users-by-email/{email}")
    User getUserByEmail(@PathVariable String email);

    @PutMapping("/api/v2/users/{userId}")
    User updateUser(@PathVariable String userId, @Valid @RequestBody User user);

    @PutMapping("/api/v2/users/{userId}/password")
    User updateUserSecret(@PathVariable String userId, @RequestBody String secret);

    @DeleteMapping("/api/v2/users/{userId}")
    void deleteUser(@PathVariable String userId);

}
