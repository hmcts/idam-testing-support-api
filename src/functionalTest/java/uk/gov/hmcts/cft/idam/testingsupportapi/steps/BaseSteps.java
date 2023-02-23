package uk.gov.hmcts.cft.idam.testingsupportapi.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.rest.SerenityRest;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cft.idam.testingsupportapi.config.EnvConfig;

public abstract class BaseSteps {

    private static String testingServiceClientToken;

    public RequestSpecification given() {
        return SerenityRest.given().baseUri(EnvConfig.TESTING_SUPPORT_API_URL);
    }

    @Given("a testing service token")
    public String givenTestingServiceClientToken() {
        if (testingServiceClientToken == null) {
            testingServiceClientToken = SerenityRest.given().baseUri(EnvConfig.PUBLIC_URL)
                .contentType(ContentType.URLENC)
                .queryParam("client_id", EnvConfig.TESTING_SERVICE_CLIENT)
                .queryParam("client_secret", EnvConfig.TESTING_SERVICE_CLIENT_SECRET)
                .queryParam("scope", "profile")
                .queryParam("grant_type", "client_credentials").post("/o/token")
                .then().assertThat().statusCode(HttpStatus.OK.value())
                .and().extract().response().path("access_token");
        }
        return testingServiceClientToken;
    }

    protected String getTestingServiceClientToken() {
        return testingServiceClientToken;
    }

    @Given("a random password")
    public String givenRandomPassword() {
        return RandomStringUtils.randomAlphabetic(12) + "!2";
    }


    @Then("status code is {0}")
    public void thenStatusCodeIs(HttpStatus statusCode) {
        SerenityRest.then().assertThat().statusCode(statusCode.value());
    }

}
