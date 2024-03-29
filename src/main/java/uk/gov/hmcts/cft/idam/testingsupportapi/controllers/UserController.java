package uk.gov.hmcts.cft.idam.testingsupportapi.controllers;

import io.opentelemetry.api.trace.Span;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ActivatedUserRequest;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingSessionService;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingUserService;
import uk.gov.hmcts.cft.idam.testingsupportapi.trace.TraceAttribute;

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
        Span.current().setAttribute(TraceAttribute.SESSION_KEY, session.getSessionKey())
            .setAttribute(TraceAttribute.SESSION_ID, session.getId())
            .setAttribute(TraceAttribute.SESSION_CLIENT_ID, session.getClientId())
            .setAttribute(TraceAttribute.EMAIL, request.getUser().getEmail())
            .setAttribute(TraceAttribute.ROLE_NAMES, request.getUser().getRoleNames() != null
                    ? String.join(",", request.getUser().getRoleNames()) : "nil");
        User testUser = testingUserService.createTestUser(session.getId(), request.getUser(), request.getPassword());
        Span.current().setAttribute(TraceAttribute.USER_ID, testUser.getId());
        return testUser;
    }

    @PutMapping("/test/idam/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "bearerAuth")
    public User createOrUpdateUser(@AuthenticationPrincipal @Parameter(hidden = true) Jwt principal,
                                   @PathVariable String userId, @RequestBody ActivatedUserRequest request) {
        request.getUser().setId(userId);
        TestingSession session = testingSessionService.getOrCreateSession(principal);
        Span.current().setAttribute(TraceAttribute.SESSION_KEY, session.getSessionKey())
            .setAttribute(TraceAttribute.SESSION_ID, session.getId())
            .setAttribute(TraceAttribute.SESSION_CLIENT_ID, session.getClientId())
            .setAttribute(TraceAttribute.USER_ID, userId)
            .setAttribute(TraceAttribute.EMAIL, request.getUser().getEmail());
        try {
            User testUser = testingUserService.createTestUser(
                session.getId(),
                request.getUser(),
                request.getPassword()
            );
            Span.current().setAttribute(TraceAttribute.OUTCOME, "create");
            return testUser;
        } catch (HttpStatusCodeException hsce) {
            if (hsce.getStatusCode() == HttpStatus.CONFLICT) {
                User testUser = testingUserService.updateTestUser(
                    session.getId(),
                    request.getUser(),
                    request.getPassword()
                );
                Span.current().setAttribute(TraceAttribute.OUTCOME, "update");
                return testUser;
            }
            throw hsce;
        }
    }

    @DeleteMapping("/test/idam/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    public void removeUser(@AuthenticationPrincipal @Parameter(hidden = true) Jwt principal,
                           @PathVariable String userId) {
        TestingSession session = testingSessionService.getOrCreateSession(principal);
        Span.current().setAttribute(TraceAttribute.SESSION_KEY, session.getSessionKey())
            .setAttribute(TraceAttribute.SESSION_ID, session.getId())
            .setAttribute(TraceAttribute.SESSION_CLIENT_ID, session.getClientId())
            .setAttribute(TraceAttribute.USER_ID, userId);
        testingUserService.addTestEntityToSessionForRemoval(session, userId);
    }

    @GetMapping("/test/idam/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "bearerAuth")
    public User getUserById(@AuthenticationPrincipal @Parameter(hidden = true) Jwt principal,
                            @PathVariable String userId) {
        return testingUserService.getUserByUserId(userId);
    }

    @GetMapping("/test/idam/users")
    @ResponseStatus(HttpStatus.OK)
    @SecurityRequirement(name = "bearerAuth")
    public User getUserByEmail(@AuthenticationPrincipal @Parameter(hidden = true) Jwt principal,
                               @RequestParam(name = "email") String email) {
        return testingUserService.getUserByEmail(email);
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
        Span.current().setAttribute(TraceAttribute.USER_ID, userId)
            .setAttribute(TraceAttribute.FORCE_DELETE, String.valueOf(forceDelete));
        if (forceDelete) {
            testingUserService.forceRemoveTestUser(userId);
        } else {
            testingUserService.removeTestUser(userId);
        }
    }

}
