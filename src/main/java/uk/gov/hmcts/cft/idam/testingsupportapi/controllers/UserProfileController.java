package uk.gov.hmcts.cft.idam.testingsupportapi.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ActivatedUserRequest;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingSessionService;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingUserProfileService;
import uk.gov.hmcts.cft.rd.model.UserProfile;

@RestController public class UserProfileController {

    private final TestingSessionService testingSessionService;

    private final TestingUserProfileService testingUserProfileService;

    public UserProfileController(TestingSessionService testingSessionService,
                                 TestingUserProfileService testingUserProfileService) {
        this.testingSessionService = testingSessionService;
        this.testingUserProfileService = testingUserProfileService;
    }

    @GetMapping("/test/rd/user-profiles/{userId}") @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "bearerAuth")
    public UserProfile getUserProfileById(@AuthenticationPrincipal @Parameter(hidden = true) Jwt principal,
                                          @PathVariable String userId) {
        return testingUserProfileService.getUserProfileByUserId(userId);
    }

    @PutMapping("/test/cft/users/{userId}") @ResponseStatus(HttpStatus.OK) @SecurityRequirement(name = "bearerAuth")
    public User createOrUpdateCftUser(@AuthenticationPrincipal @Parameter(hidden = true) Jwt principal,
                                      @PathVariable String userId, @RequestBody ActivatedUserRequest request)
        throws Exception {
        TestingSession session = testingSessionService.getOrCreateSession(principal);
        return testingUserProfileService.createOrUpdateCftUser(session.getId(),
                                                               request.getUser(),
                                                               request.getPassword()
        );
    }

}
