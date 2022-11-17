package uk.gov.hmcts.cft.idam.testingsupportapi.controllers;

import io.opentelemetry.api.trace.Span;
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
import uk.gov.hmcts.cft.idam.api.v2.common.model.Role;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingRoleService;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingSessionService;
import uk.gov.hmcts.cft.idam.testingsupportapi.trace.TraceAttribute;

import static uk.gov.hmcts.cft.idam.testingsupportapi.util.PrincipalHelper.getClientId;
import static uk.gov.hmcts.cft.idam.testingsupportapi.util.PrincipalHelper.getSessionKey;

@Slf4j
@RestController
public class RoleController {

    private final TestingSessionService testingSessionService;
    private final TestingRoleService testingRoleService;

    public RoleController(TestingSessionService testingSessionService, TestingRoleService testingRoleService) {
        this.testingSessionService = testingSessionService;
        this.testingRoleService = testingRoleService;
    }

    @PostMapping("/test/idam/roles")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    public Role createRole(@AuthenticationPrincipal @Parameter(hidden = true) Jwt principal,
                           @RequestBody Role requestRole) {
        TestingSession session = testingSessionService.getOrCreateSession(principal);
        Span.current()
            .setAttribute(TraceAttribute.SESSION_KEY, session.getSessionKey())
            .setAttribute(TraceAttribute.SESSION_CLIENT_ID, session.getClientId())
            .setAttribute(TraceAttribute.ROLE_NAME, requestRole.getName());
        return testingRoleService.createTestRole(session.getId(), requestRole);

    }

}
