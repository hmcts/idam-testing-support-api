package uk.gov.hmcts.cft.idam.testingsupportapi;

import io.restassured.response.Response;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.thucydides.core.annotations.Steps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.steps.UserSteps;

@ExtendWith(SerenityJUnit5Extension.class)
public class UserApiIntegrationTest {

    @Steps
    private UserSteps userSteps;

    @Test
    public void test() {
        User user = userSteps.givenNewUser();
        String password = userSteps.givenRandomPassword();
        User response = userSteps.createUserWithPassword(user, password);
        userSteps.thenStatusCodeIsCreated();
    }

}
