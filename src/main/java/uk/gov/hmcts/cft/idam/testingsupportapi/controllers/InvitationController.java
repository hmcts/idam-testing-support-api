package uk.gov.hmcts.cft.idam.testingsupportapi.controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cft.idam.api.v2.common.model.Invitation;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingInvitationService;

import java.util.List;

@Slf4j
@RestController
public class InvitationController {

    private final TestingInvitationService testingInvitationService;

    public InvitationController(TestingInvitationService testingInvitationService) {
        this.testingInvitationService = testingInvitationService;
    }

    @GetMapping("/test/idam/invitations")
    @SecurityRequirement(name = "bearerAuth")
    List<Invitation> getInvitationsByUserEmail(@RequestParam String email) {
        return testingInvitationService.getInvitationsByUserEmail(email);
    }
}
