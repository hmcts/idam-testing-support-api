package uk.gov.hmcts.cft.idam.testingsupportapi.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ActivatedUserRequest;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingSessionService;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingUserService;
import uk.gov.hmcts.cft.rpe.api.RpeS2STestingSupportApi;

import static uk.gov.hmcts.cft.idam.testingsupportapi.util.PrincipalHelper.getClientId;
import static uk.gov.hmcts.cft.idam.testingsupportapi.util.PrincipalHelper.getSessionKey;

@Slf4j
@RestController
public class UserController {

    private final TestingSessionService testingSessionService;
    private final TestingUserService testingUserService;

    private final RpeS2STestingSupportApi rpeS2STestingSupportApi;

    public UserController(TestingSessionService testingSessionService, TestingUserService testingUserService,
                          RpeS2STestingSupportApi rpeS2STestingSupportApi) {
        this.testingSessionService = testingSessionService;
        this.testingUserService = testingUserService;
        this.rpeS2STestingSupportApi = rpeS2STestingSupportApi;
    }

    @PostMapping("/test/idam/users")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    public User createUser(@AuthenticationPrincipal @Parameter(hidden = true) Jwt principal,
                           @RequestBody ActivatedUserRequest request) {
        String sessionKey = getSessionKey(principal);
        String clientId = getClientId(principal).orElse("unknown");
        log.info("Create user '{}' for client '{}', session '{}'", request.getUser().getEmail(), clientId, sessionKey);
        TestingSession session = testingSessionService.getOrCreateSession(sessionKey, clientId);
        return testingUserService.createTestUser(session.getId(), request.getUser(), request.getPassword());
    }

    @DeleteMapping("/test/idam/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    public void removeUser(@AuthenticationPrincipal @Parameter(hidden = true) Jwt principal,
                           @PathVariable String userId) {
        String sessionKey = getSessionKey(principal);
        String clientId = getClientId(principal).orElse("unknown");
        TestingSession session = testingSessionService.getOrCreateSession(sessionKey, clientId);
        testingUserService.addTestUserToSessionForRemoval(session, userId);
    }

    @PostMapping("/test/idam/burner/users")
    @ResponseStatus(HttpStatus.CREATED)
    public User createBurnerUser(@RequestBody ActivatedUserRequest request) {
        log.info("Create burner user '{}'", request.getUser().getEmail());
        return testingUserService.createTestUser(null, request.getUser(), request.getPassword());
    }

    @DeleteMapping("/test/idam/burner/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBurnerUser(@PathVariable String userId,
                                 @RequestHeader(value = "force", required = false) boolean forceDelete) {
        if (forceDelete) {
            testingUserService.forceRemoveTestUser(userId);
        } else {
            testingUserService.removeTestUser(userId);
        }
    }

    @PostMapping("/test/prd/users")
    @ResponseStatus(HttpStatus.CREATED)
    public String createProfessionalUser(@RequestBody ActivatedUserRequest request) {
        return rpeS2STestingSupportApi.lease("rd_professional_api");
    }

}
