package uk.gov.hmcts.cft.idam.testingsupportapi.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ActivatedUserRequest;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.service.notify.Notification;

import static org.springframework.http.ResponseEntity.ok;

/**
 * Default endpoints per application.
 */
@Slf4j
@RestController
public class RootController {

    @Value("${azure.application-insights.instrumentation-key}")
    private String appInsightKey;

    @GetMapping("/")
    public ResponseEntity<String> welcome() {
        log.info("Welcome to idam-testing-support-api application! app insight key is '{}'", appInsightKey);
        return ok("Welcome to idam-testing-support-api application! " + appInsightKey);
    }

    @PostMapping("/test/idam/users")
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody ActivatedUserRequest request) {
        log.info("Create test user {}", request.getUser().getEmail());
        return request.getUser();
    }

    @DeleteMapping("/test/idam/users/{userId}")
    public User deleteUser(@PathVariable String userId) {
        return null;
    }

    @GetMapping("/test/idam/notifications/latest/{emailAddress}")
    public Notification getLatestNotification(@PathVariable String emailAddress) {
        return null;
    }

    @PostMapping("/test/idam/burner/users")
    @ResponseStatus(HttpStatus.CREATED)
    public User createBurnerUser(@RequestBody ActivatedUserRequest request) {
        log.info("Create test burner user {}", request.getUser().getEmail());
        return request.getUser();
    }
}
