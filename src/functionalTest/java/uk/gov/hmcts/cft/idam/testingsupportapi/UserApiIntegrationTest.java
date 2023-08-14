package uk.gov.hmcts.cft.idam.testingsupportapi;

import net.serenitybdd.junit.runners.SerenityRunner;
import net.thucydides.core.annotations.Title;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.steps.UserSteps;

@RunWith(SerenityRunner.class)
public class UserApiIntegrationTest {

    private UserSteps userSteps = new UserSteps();

    @BeforeEach
    public void setup() {
        userSteps.givenTestingServiceClientToken();
    }

    @Test
    @Title("Create test user successfully")
    public void testCreateTestUserSuccess() {
        User user = userSteps.givenNewUserDetails();
        String password = userSteps.givenRandomPassword();
        userSteps.createTestUserWithPassword(user, password);
        userSteps.thenStatusCodeIs(HttpStatus.CREATED);
    }

}
