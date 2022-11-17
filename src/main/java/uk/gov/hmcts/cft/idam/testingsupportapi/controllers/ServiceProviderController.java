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
import uk.gov.hmcts.cft.idam.api.v2.common.model.ServiceProvider;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingServiceProviderService;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingSessionService;
import uk.gov.hmcts.cft.idam.testingsupportapi.trace.TraceAttribute;

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
            .setAttribute(TraceAttribute.SESSION_CLIENT_ID, session.getClientId())
            .setAttribute(TraceAttribute.CLIENT_ID, serviceProvider.getClientId());
        return testingServiceProviderService.createService(session.getId(), serviceProvider);

    }
}
