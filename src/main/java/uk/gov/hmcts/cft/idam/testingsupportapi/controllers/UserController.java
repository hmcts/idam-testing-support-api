package uk.gov.hmcts.cft.idam.testingsupportapi.controllers;

import io.opentelemetry.api.trace.Span;
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
import uk.gov.hmcts.cft.idam.testingsupportapi.trace.TraceAttribute;

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
        TestingSession session = testingSessionService.getOrCreateSession(principal);
        Span.current()
            .setAttribute(TraceAttribute.SESSION_KEY, session.getSessionKey())
            .setAttribute(TraceAttribute.CLIENT_ID, session.getClientId())
            .setAttribute(TraceAttribute.EMAIL, request.getUser().getEmail());
        User testUser = testingUserService.createTestUser(session.getId(), request.getUser(), request.getPassword());
        Span.current().setAttribute(TraceAttribute.USER_ID, testUser.getId());
        return testUser;
    }

    @DeleteMapping("/test/idam/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    public void removeUser(@AuthenticationPrincipal @Parameter(hidden = true) Jwt principal,
                           @PathVariable String userId) {
        TestingSession session = testingSessionService.getOrCreateSession(principal);
        Span.current()
            .setAttribute(TraceAttribute.SESSION_KEY, session.getSessionKey())
            .setAttribute(TraceAttribute.CLIENT_ID, session.getClientId())
            .setAttribute(TraceAttribute.USER_ID, userId);
        testingUserService.addTestUserToSessionForRemoval(session, userId);
    }

    @PostMapping("/test/idam/burner/users")
    @ResponseStatus(HttpStatus.CREATED)
    public User createBurnerUser(@RequestBody ActivatedUserRequest request) {
        Span.current().setAttribute(TraceAttribute.EMAIL, request.getUser().getEmail());
        User testUser = testingUserService.createTestUser(null, request.getUser(), request.getPassword());
        Span.current().setAttribute(TraceAttribute.USER_ID, testUser.getId());
        return testUser;
    }

    @DeleteMapping("/test/idam/burner/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBurnerUser(@PathVariable String userId,
                                 @RequestHeader(value = "force", required = false) boolean forceDelete) {
        Span.current().setAttribute(TraceAttribute.USER_ID, userId);
        if (forceDelete) {
            testingUserService.forceRemoveTestUser(userId);
        } else {
            testingUserService.removeTestUser(userId);
        }
    }

}
