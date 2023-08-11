package uk.gov.hmcts.cft.idam.testingsupportapi;

//import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.thucydides.core.annotations.Steps;
import net.thucydides.core.annotations.Title;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.steps.UserSteps;

@ExtendWith(SerenityJUnit5Extension.class)
public class UserApiIntegrationTest {

    @Steps
    private UserSteps userSteps;

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
