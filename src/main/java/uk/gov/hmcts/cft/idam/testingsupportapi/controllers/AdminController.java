package uk.gov.hmcts.cft.idam.testingsupportapi.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.AdminService;

@Slf4j
@RestController
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/trigger/expiry/burner/users")
    @ResponseStatus(HttpStatus.OK)
    public void triggerExpiryBurnerUsers() {
        adminService.triggerExpiryBurnerUsers();
    }

    @PostMapping("/trigger/expiry/sessions")
    @ResponseStatus(HttpStatus.OK)
    public void triggerExpirySessions() {
        adminService.triggerExpirySessions();
    }

    @DeleteMapping("/admin/entities/users")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteUserTestingEntity(@RequestBody CleanupEntity testingEntity) {
        adminService.cleanupUser(testingEntity);
    }

    @DeleteMapping("/admin/sessions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteSession(@RequestBody CleanupSession testingSession) {
        adminService.cleanupSession(testingSession);
    }

    @DeleteMapping("/admin/entities/roles")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteRoleTestingEntity(@RequestBody CleanupEntity testingEntity) {
        adminService.cleanupRole(testingEntity);
    }


}
