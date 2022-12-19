package uk.gov.hmcts.cft.rd.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.cft.rd.api.auth.RDAuthConfig2;
import uk.gov.hmcts.cft.rd.model.UserProfile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@FeignClient(name = "rduserprofileapi", url = "${rd.userprofile.api.url}", configuration = RDAuthConfig2.class)
public interface RDUserProfileApi {

    String USER_IDS = "userIds";

    @GetMapping("/v1/userprofile")
    UserProfile getUserProfileById(@RequestParam("userId") String id);

    @GetMapping("/v1/userprofile")
    UserProfile getUserProfileByEmail(@RequestParam("email") String email);

    @PostMapping("/v1/userprofile")
    UserProfile createUserProfile(@RequestBody UserProfile userProfile);

    default void deleteUserProfile(String id) {
        deleteUserProfile(Map.of(USER_IDS, Collections.singletonList(id)));
    }

    @DeleteMapping("/v1/userprofile")
    void deleteUserProfile(@RequestBody Map<String, List<String>> body);

}
