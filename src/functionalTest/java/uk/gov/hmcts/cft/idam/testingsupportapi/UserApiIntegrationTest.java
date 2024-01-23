package uk.gov.hmcts.cft.idam.testingsupportapi;

import net.serenitybdd.annotations.Steps;
import net.serenitybdd.annotations.Title;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cft.idam.api.v2.common.model.RecordType;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.steps.UserSteps;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    @Title("Create archived test user successfully")
    public void testCreateArchivedTestUserSuccess() {
        User user = userSteps.givenNewUserDetails();
        user.setRecordType(RecordType.ARCHIVED);
        String password = userSteps.givenRandomPassword();
        userSteps.createTestUserWithPassword(user, password);
        userSteps.thenStatusCodeIs(HttpStatus.CREATED);
        User testUser = userSteps.thenGetUserFromResponse();
        assertEquals(RecordType.ARCHIVED, testUser.getRecordType());
    }

}
