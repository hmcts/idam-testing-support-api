package uk.gov.hmcts.cft.idam.testingsupportapi.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ActivatedUserRequest;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingUserProfessionalService;

@Slf4j
@RestController
public class UserProfessionalController {

    private final TestingUserProfessionalService testingUserProfessionalService;

    public UserProfessionalController(TestingUserProfessionalService testingUserProfessionalService) {
        this.testingUserProfessionalService = testingUserProfessionalService;
    }

    @PostMapping("/test/prd/users")
    @ResponseStatus(HttpStatus.CREATED)
    public User createProfessionalUser(@RequestBody ActivatedUserRequest request) throws Exception {
        return testingUserProfessionalService.createTestUser(null, request.getUser(), request.getPassword());
    }

}
