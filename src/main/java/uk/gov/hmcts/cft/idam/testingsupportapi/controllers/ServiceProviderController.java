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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ServiceProvider;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingServiceProviderService;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingSessionService;
import uk.gov.hmcts.cft.idam.testingsupportapi.trace.TraceAttribute;

import java.util.List;

@Slf4j
@RestController
public class ServiceProviderController {

    private final TestingSessionService testingSessionService;
    private final TestingServiceProviderService testingServiceProviderService;

    public ServiceProviderController(TestingSessionService testingSessionService,
                                     TestingServiceProviderService testingServiceProviderService) {
        this.testingSessionService = testingSessionService;
        this.testingServiceProviderService = testingServiceProviderService;
    }

    @PostMapping("/test/idam/services")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    public ServiceProvider createService(@AuthenticationPrincipal @Parameter(hidden = true) Jwt principal,
                                         @RequestBody ServiceProvider serviceProvider) {
        TestingSession session = testingSessionService.getOrCreateSession(principal);
        Span.current()
            .setAttribute(TraceAttribute.SESSION_KEY, session.getSessionKey())
            .setAttribute(TraceAttribute.SESSION_ID, session.getId())
            .setAttribute(TraceAttribute.SESSION_CLIENT_ID, session.getClientId())
            .setAttribute(TraceAttribute.CLIENT_ID, serviceProvider.getClientId());
        try {
            return testingServiceProviderService.createService(session.getId(), serviceProvider);
        } catch (HttpStatusCodeException hsce) {
            if (hsce.getStatusCode() == HttpStatus.CONFLICT) {
                testingServiceProviderService.findAllActiveByEntityId(serviceProvider.getClientId()).forEach(te -> {
                    testingServiceProviderService.detachEntity(te.getId());
                });
                Span.current().setAttribute(TraceAttribute.OUTCOME, "detached");
            }
            throw hsce;
        }

    }

    @DeleteMapping("/test/idam/services/{clientId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    public void removeService(@AuthenticationPrincipal @Parameter(hidden = true) Jwt principal,
                           @PathVariable String clientId) {
        TestingSession session = testingSessionService.getOrCreateSession(principal);
        Span.current()
            .setAttribute(TraceAttribute.SESSION_KEY, session.getSessionKey())
            .setAttribute(TraceAttribute.SESSION_ID, session.getId())
            .setAttribute(TraceAttribute.SESSION_CLIENT_ID, session.getClientId())
            .setAttribute(TraceAttribute.CLIENT_ID, clientId);
        testingServiceProviderService.addTestEntityToSessionForRemoval(session, clientId);
    }
}
