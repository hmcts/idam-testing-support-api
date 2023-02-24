package uk.gov.hmcts.cft.idam.testingsupportapi.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import net.serenitybdd.rest.SerenityRest;
import org.apache.commons.lang3.RandomStringUtils;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ActivatedUserRequest;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;

import java.util.Collections;

public class UserSteps extends BaseSteps {

    @Given("new user details")
    public User givenNewUserDetails() {
        String randomString = RandomStringUtils.randomAlphabetic(12);
        User user = new User();
        user.setEmail(randomString + "@functional.local");
        user.setForename(randomString);
        user.setSurname(randomString);
        user.setRoleNames(Collections.singletonList("citizen"));
        return user;
    }

    @When("create test user with password")
    public void createTestUserWithPassword(User user, String password) {
        ActivatedUserRequest activatedUserRequest = new ActivatedUserRequest();
        activatedUserRequest.setUser(user);
        activatedUserRequest.setPassword(password);

        String token = getTestingServiceClientToken();

        given()
            .header("authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(activatedUserRequest)
            .post("/test/idam/users");
    }

    @Then("get user from response")
    public User thenGetUserFromResponse() {
        return SerenityRest.then().extract().body().as(User.class);
    }

}
