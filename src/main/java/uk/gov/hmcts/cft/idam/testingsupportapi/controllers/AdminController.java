package uk.gov.hmcts.cft.idam.testingsupportapi.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.AdminService;

@Slf4j
@RestController
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/admin/check/expiry")
    @ResponseStatus(HttpStatus.OK)
    public void checkExpiry() {
        adminService.checkExpiry();
    }

}
