package uk.gov.hmcts.cft.idam.testingsupportapi.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ApiError;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ErrorDetail;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommonExceptionHandlerTest {

    private final CommonExceptionHandler underTest = new CommonExceptionHandler();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private HttpServerErrorException mockException;
    @Mock
    private HttpServletRequest mockRequest;

    /**
     * @verifies convert HttpStatusCodeException to error response with single message
     * @see CommonExceptionHandler#handle(org.springframework.web.client.HttpStatusCodeException, javax.servlet.http.HttpServletRequest)
     */
    @Test
    void handle_shouldConvertHttpStatusCodeExceptionToErrorResponseWithSingleMessage() {
        when(mockException.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
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
    void handle_shouldConvertHttpStatusCodeExceptionToErrorResponseWithDetailsFromBody() throws Exception {

        Map<String, String> body = ImmutableMap.of("testkey", "test-body-error");

        when(mockException.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
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

    @Test
    void handle_shouldConvertTrustedExceptionBodyToErrorResponseWithSingleMessage() throws Exception {
        ErrorDetail errorDetail = new ErrorDetail("test-path", "test-code", "test-message");
        when(mockException.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        when(mockException.getResponseBodyAsString())
            .thenReturn(objectMapper.writeValueAsString(List.of(errorDetail)));
        when(mockException.getMessage()).thenReturn(SpringWebClientHelper.ERROR_DETAIL_MARKER + " test-error-message");
        when(mockRequest.getMethod()).thenReturn("POST");
        when(mockRequest.getRequestURI()).thenReturn("/test-uri");

        ResponseEntity<ApiError> result = underTest.handle(mockException, mockRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getBody().getStatus());
        assertEquals("POST", result.getBody().getMethod());
        assertNull(result.getBody().getErrors());
        assertEquals(1, result.getBody().getDetails().size());
        assertEquals("test-path", result.getBody().getDetails().get(0).getPath());
        assertEquals("test-code", result.getBody().getDetails().get(0).getCode());
        assertEquals("test-message", result.getBody().getDetails().get(0).getMessage());

    }
}
