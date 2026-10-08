package uk.gov.hmcts.cft.idam.testingsupportapi.error;

import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientErrorDecoder;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ApiError;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class UpstreamApiErrorTest {

    private final CommonExceptionHandler handler = new CommonExceptionHandler();

    @ParameterizedTest
    @ValueSource(ints = {400, 403, 404, 409, 412, 429, 500, 502})
    void preservesDiagnosticsAndUsesLocalRequestContext(int status) {
        HttpStatusCodeException exception = decode(status, """
            {
              "timestamp": "2000-01-01T00:00:00Z",
              "status": 200,
              "method": "PUT",
              "path": "/api/v2/users/123",
              "errors": ["First error", "Second error"],
              "details": [
                {"path": "user.email", "code": "NOT_UNIQUE", "message": "Email already exists"},
                {"path": null, "code": "ERROR", "message": "Échec", "futureField": true},
                {"code": "ERROR", "message": "Missing path is allowed"}
              ],
              "futureField": {"value": true}
            }
            """);
        Instant before = Instant.now();
        var response = handler.handle(exception, new MockHttpServletRequest("POST", "/test/idam/users"));
        ApiError body = response.getBody();

        assertEquals(status, response.getStatusCode().value());
        assertEquals(status, body.getStatus());
        assertEquals("POST", body.getMethod());
        assertEquals("/test/idam/users", body.getPath());
        assertFalse(body.getTimestamp().isBefore(before));
        assertFalse(body.getTimestamp().isAfter(Instant.now()));
        assertEquals(List.of("First error", "Second error"), body.getErrors());
        assertEquals(3, body.getDetails().size());
        assertEquals("user.email", body.getDetails().get(0).getPath());
        assertEquals("NOT_UNIQUE", body.getDetails().get(0).getCode());
        assertEquals("Email already exists", body.getDetails().get(0).getMessage());
        assertNull(body.getDetails().get(1).getPath());
        assertEquals("Échec", body.getDetails().get(1).getMessage());
        assertNull(body.getDetails().get(2).getPath());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{\"errors\":[\"Helpful message\"]}",
        "{\"errors\":[\"Helpful message\"],\"details\":null}",
        "{\"errors\":[\"Helpful message\"],\"details\":[]}"
    })
    void preservesErrorsWithoutDetails(String json) {
        ApiError result = handle(decode(500, json));
        assertEquals(List.of("Helpful message"), result.getErrors());
        assertNull(result.getDetails());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "\"errors\":null,", "\"errors\":[],"})
    void preservesDetailsWithoutErrors(String errors) {
        ApiError result = handle(decode(400, "{" + errors + """
            "details":[{"path":"password","code":"BAD_VALUE","message":"Invalid password"}]}
            """));
        assertNull(result.getErrors());
        assertEquals("Invalid password", result.getDetails().get(0).getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        " ", "null", "{}", "[]", "not json", "<html>Bad gateway</html>", "{",
        "{\"errors\":[],\"details\":[]}",
        "{\"errors\":null,\"details\":null}",
        "{\"errors\":[123]}", "{\"errors\":[null]}", "{\"errors\":[\" \"]}",
        "{\"details\":{}}", "{\"details\":[null]}", "{\"details\":[{}]}",
        "{\"details\":[{\"message\":42}]}",
        "{\"details\":[{\"message\":\"Error\",\"path\":{}}]}",
        "{\"details\":[{\"message\":\"Error\",\"code\":42}]}",
        "{\"errors\":[\"Error\"]} {}"
    })
    void fallsBackForUnusableBodies(String json) {
        HttpStatusCodeException exception = decode(502, json);
        ApiError result = handle(exception);
        assertEquals(502, result.getStatus());
        assertEquals(List.of(exception.getMessage()), result.getErrors());
        assertNull(result.getDetails());
    }

    @Test
    void keepsLegacyFlatMapFallbackNullSafe() {
        HttpStatusCodeException exception = decode(400, """
            {"status":400,"message":"Legacy error","optional":null}
            """);
        assertEquals(List.of(exception.getMessage(), "Legacy error"), handle(exception).getErrors());
    }

    @Test
    void keepsExistingExceptionTypesForStatusDependentFlows() {
        assertInstanceOf(HttpClientErrorException.NotFound.class, decode(404, "{}"));
        assertInstanceOf(HttpClientErrorException.Conflict.class, decode(409, "{}"));
        assertInstanceOf(HttpClientErrorException.class, decode(412, "{}"));
    }

    private ApiError handle(HttpStatusCodeException exception) {
        return handler.handle(exception, new MockHttpServletRequest("POST", "/test/idam/users")).getBody();
    }

    private HttpStatusCodeException decode(int status, String json) {
        Request request = Request.create(Request.HttpMethod.POST, "http://idam-api/api/v2/users",
                                         Map.of(), null, UTF_8, null);
        Response response = Response.builder().request(request).status(status)
            .reason(HttpStatus.valueOf(status).getReasonPhrase()).headers(Map.of())
            .body(json, UTF_8).build();
        return (HttpStatusCodeException) new SpringWebClientErrorDecoder()
            .decode("IdamV2UserManagementApi#createUser(ActivatedUserRequest)", response);
    }
}
