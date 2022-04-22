package uk.gov.hmcts.cft.idam.testingsupportapi.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.NotificationsService;
import uk.gov.service.notify.Notification;

@Slf4j
@RestController
public class NotificationsController {

    private final NotificationsService notificationsService;

    public NotificationsController(NotificationsService notificationsService) {
        this.notificationsService = notificationsService;
    }

    @GetMapping("/test/idam/notifications/latest/{emailAddress}")
    @SecurityRequirement(name = "bearerAuth")
    public Notification getLatestNotification(@AuthenticationPrincipal @Parameter(hidden = true) Jwt principal,
                                              @PathVariable String emailAddress) throws Exception {
        return notificationsService.findLatestNotification(emailAddress)
            .orElseThrow(SpringWebClientHelper::notFound);
    }

}
