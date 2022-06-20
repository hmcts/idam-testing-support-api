package uk.gov.hmcts.cft.idam.testingsupportapi.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ActivatedUserRequest;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.model.UserTestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingSessionService;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingUserService;

import static uk.gov.hmcts.cft.idam.testingsupportapi.util.PrincipalHelper.getClientId;
import static uk.gov.hmcts.cft.idam.testingsupportapi.util.PrincipalHelper.getSessionKey;

@Slf4j
@RestController
public class UserController {

    private final TestingSessionService testingSessionService;
    private final TestingUserService testingUserService;

    public UserController(TestingSessionService testingSessionService, TestingUserService testingUserService) {
        this.testingSessionService = testingSessionService;
        this.testingUserService = testingUserService;
    }

    @PostMapping("/test/idam/users")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    public User createUser(@AuthenticationPrincipal @Parameter(hidden = true) Jwt principal,
                           @RequestBody ActivatedUserRequest request) {
        String sessionKey = getSessionKey(principal);
        String clientId = getClientId(principal).orElse("unknown");
        TestingSession session = testingSessionService.getOrCreateSession(sessionKey, clientId);
        UserTestingEntity result = testingUserService.createTestUser(
            session.getId(),
            request.getUser(),
            request.getPassword()
        );
        return result.getUser();
    }

    @PostMapping("/test/idam/burner/users")
    @ResponseStatus(HttpStatus.CREATED)
    public User createBurnerUser(@RequestBody ActivatedUserRequest request) {
        UserTestingEntity result = testingUserService.createTestUser(
            null,
            request.getUser(),
            request.getPassword()
        );
        return result.getUser();
    }

}
