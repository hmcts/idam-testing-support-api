package uk.gov.hmcts.cft.idam.testingsupportapi.controllers;

import io.opentelemetry.api.trace.Span;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cft.idam.api.v2.common.model.Invitation;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingInvitationService;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingSessionService;
import uk.gov.hmcts.cft.idam.testingsupportapi.trace.TraceAttribute;

import java.util.List;

@Slf4j
@RestController
public class InvitationController {

    private final TestingSessionService testingSessionService;

    private final TestingInvitationService testingInvitationService;

    public InvitationController(TestingSessionService testingSessionService,
                                TestingInvitationService testingInvitationService) {
        this.testingSessionService = testingSessionService;
        this.testingInvitationService = testingInvitationService;
    }

    @GetMapping("/test/idam/invitations")
    @SecurityRequirement(name = "bearerAuth")
    public List<Invitation> getInvitationsByUserEmail(@RequestParam String email) {
        return testingInvitationService.getInvitationsByUserEmail(email);
    }

    @PostMapping("/test/idam/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    public Invitation createInvitation(@AuthenticationPrincipal @Parameter(hidden = true) Jwt principal,
                                       @RequestBody Invitation invitation) {
        TestingSession session = testingSessionService.getOrCreateSession(principal);
        Span.current().setAttribute(TraceAttribute.SESSION_KEY, session.getSessionKey())
            .setAttribute(TraceAttribute.SESSION_ID, session.getId())
            .setAttribute(TraceAttribute.SESSION_CLIENT_ID, session.getClientId())
            .setAttribute(TraceAttribute.EMAIL, invitation.getEmail());
        Invitation result = testingInvitationService.createTestInvitation(session.getId(), invitation);
        Span.current().setAttribute(TraceAttribute.INVITATION_ID, result.getId());
        return result;
    }
}
