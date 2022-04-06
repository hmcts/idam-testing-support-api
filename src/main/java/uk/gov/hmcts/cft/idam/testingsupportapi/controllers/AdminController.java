package uk.gov.hmcts.cft.idam.testingsupportapi.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class AdminController {

    @GetMapping("/admin/check/expiry")
    @ResponseStatus(HttpStatus.OK)
    public void checkExpiry() {
        log.info("I will look for things that have expired");
    }

}
