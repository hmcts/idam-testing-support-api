package uk.gov.hmcts.cft.idam.testingsupportapi.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.EncoderConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.rest.SerenityRest;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.config.EnvConfig;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

public abstract class BaseSteps {

    private static final RequestSpecification defaultSpec = requestSpecification(EnvConfig.TESTING_SUPPORT_API_URL);

    private static String testingServiceClientToken;

    private static RequestSpecification requestSpecification(String baseUri) {
        final RestAssuredConfig config = RestAssuredConfig.newConfig()
            .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset(StandardCharsets.UTF_8));
        final RequestSpecBuilder specBuilder = new RequestSpecBuilder().setConfig(config).setBaseUri(baseUri)
            .setRelaxedHTTPSValidation();
        return specBuilder.build();
    }

    public RequestSpecification given() {
        return SerenityRest.given(defaultSpec);
    }

    public String getTestingServiceClientToken() {
        if (testingServiceClientToken == null) {
            testingServiceClientToken = SerenityRest
                .given().baseUri(EnvConfig.PUBLIC_URL).relaxedHTTPSValidation().contentType(ContentType.URLENC)
                .queryParam("client_id", EnvConfig.TESTING_SERVICE_CLIENT)
                .queryParam("client_secret", EnvConfig.TESTING_SERVICE_CLIENT_SECRET)
                .queryParam("scope", "profile")
                .queryParam("grant_type", "client_credentials")
                .post("/o/token")
                .then().assertThat().statusCode(HttpStatus.OK.value())
                .and().extract().response().path("access_token");
        }
        return testingServiceClientToken;
    }

    @Given("a new user")
    public User givenNewUser() {
        String randomString = RandomStringUtils.randomAlphabetic(12);
        User user = new User();
        user.setEmail(randomString + "@functional.local");
        user.setForename(randomString);
        user.setSurname(randomString);
        user.setRoleNames(Collections.singletonList("citizen"));
        return user;
    }

    @Given("a random password")
    public String givenRandomPassword() {
        return RandomStringUtils.randomAlphabetic(12) + "!2";
    }


    @Then("status code is created")
    public void thenStatusCodeIsCreated() {
        SerenityRest.then().assertThat().statusCode(HttpStatus.CREATED.value());
    }

}
