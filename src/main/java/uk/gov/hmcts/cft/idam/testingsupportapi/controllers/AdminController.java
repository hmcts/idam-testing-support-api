package uk.gov.hmcts.cft.idam.testingsupportapi.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.AdminService;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingUserService;

import java.util.List;

@Slf4j
@RestController
public class AdminController {

    private final AdminService adminService;

    private final TestingUserService testingUserService;

    public AdminController(AdminService adminService, TestingUserService testingUserService) {
        this.adminService = adminService;
        this.testingUserService = testingUserService;
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

    @DeleteMapping("/admin/entities/services")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteServiceTestingEntity(@RequestBody CleanupEntity testingEntity) {
        adminService.cleanupService(testingEntity);
    }

    @GetMapping("/admin/sessions/{sessionId}/dependencies")
    @ResponseStatus(HttpStatus.OK)
    List<TestingEntity> getSessionDependencies(@PathVariable String sessionId) {
        return testingUserService.getTestingEntitiesForSessionById(sessionId);
    }


}
