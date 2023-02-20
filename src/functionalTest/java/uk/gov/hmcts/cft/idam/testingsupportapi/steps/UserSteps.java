package uk.gov.hmcts.cft.idam.testingsupportapi.steps;

import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ActivatedUserRequest;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.config.EnvConfig;

import static net.serenitybdd.rest.SerenityRest.when;

public class UserSteps extends BaseSteps {

    @When("create user with password")
    public User createUserWithPassword(User user, String password) {
        ActivatedUserRequest activatedUserRequest = new ActivatedUserRequest();
        activatedUserRequest.setUser(user);
        activatedUserRequest.setPassword(password);

        String token = getTestingServiceClientToken();

        return given()
            .header("authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(activatedUserRequest)
            .post("/test/idam/users")
            .then()
            .extract().body().as(User.class);
    }

}
