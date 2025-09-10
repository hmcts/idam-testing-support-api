package uk.gov.hmcts.cft.idam.testingsupportapi.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import net.serenitybdd.rest.SerenityRest;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
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

    @When("create test user with password")
    public void updateTestUserWithPassword(User user, String password) {
        ActivatedUserRequest activatedUserRequest = new ActivatedUserRequest();
        activatedUserRequest.setUser(user);
        activatedUserRequest.setPassword(password);

        String token = getTestingServiceClientToken();

        given()
            .header("authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(activatedUserRequest)
            .put("/test/idam/users/" + user.getId());
    }

    @Then("get user from response")
    public User thenGetUserFromResponse() {
        return SerenityRest.then().extract().body().as(User.class);
    }

    @When("get user by email {0}")
    public User getUserByEmail(String email) {
        String token = getTestingServiceClientToken();

        return given()
            .header("authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .get("/test/idam/users?email=" + email)
            .then().assertThat().statusCode(HttpStatus.OK.value())
            .extract().body().as(User.class);
    }

}
