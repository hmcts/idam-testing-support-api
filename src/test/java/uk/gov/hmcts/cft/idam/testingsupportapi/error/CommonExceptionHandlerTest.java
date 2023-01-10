package uk.gov.hmcts.cft.idam.testingsupportapi.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ApiError;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommonExceptionHandlerTest {

    @Mock
    private HttpServerErrorException mockException;

    @Mock
    private HttpServletRequest mockRequest;

    private final CommonExceptionHandler underTest = new CommonExceptionHandler();

    private final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * @verifies convert HttpStatusCodeException to error response with single message
     * @see CommonExceptionHandler#handle(org.springframework.web.client.HttpStatusCodeException, javax.servlet.http.HttpServletRequest)
     */
    @Test
    public void handle_shouldConvertHttpStatusCodeExceptionToErrorResponseWithSingleMessage() throws Exception {
        when(mockException.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        when(mockException.getRawStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR.value());
        when(mockException.getResponseBodyAsByteArray()).thenReturn(null);
        when(mockException.getMessage()).thenReturn("test-error-message");
        when(mockRequest.getMethod()).thenReturn("POST");
        when(mockRequest.getRequestURI()).thenReturn("/test-uri");

        ResponseEntity<ApiError> result = underTest.handle(mockException, mockRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getBody().getStatus());
        assertEquals("POST", result.getBody().getMethod());
        assertEquals(1, result.getBody().getErrors().size());
        assertEquals("test-error-message", result.getBody().getErrors().get(0));
    }

    /**
     * @verifies convert HttpStatusCodeException to error response with details from body
     * @see CommonExceptionHandler#handle(org.springframework.web.client.HttpStatusCodeException, javax.servlet.http.HttpServletRequest)
     */
    @Test
    public void handle_shouldConvertHttpStatusCodeExceptionToErrorResponseWithDetailsFromBody() throws Exception {

        Map<String, String> body = ImmutableMap.of("testkey", "test-body-error");

        when(mockException.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        when(mockException.getRawStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR.value());
        when(mockException.getResponseBodyAsByteArray()).thenReturn(objectMapper.writeValueAsBytes(body));
        when(mockException.getMessage()).thenReturn("test-error-message");
        when(mockRequest.getMethod()).thenReturn("POST");
        when(mockRequest.getRequestURI()).thenReturn("/test-uri");

        ResponseEntity<ApiError> result = underTest.handle(mockException, mockRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getBody().getStatus());
        assertEquals("POST", result.getBody().getMethod());
        assertEquals(2, result.getBody().getErrors().size());
        assertEquals("test-error-message", result.getBody().getErrors().get(0));
        assertEquals("test-body-error", result.getBody().getErrors().get(1));
    }
}
