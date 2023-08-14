package uk.gov.hmcts.cft.idam.testingsupportapi.error;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ApiError;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper.convertJsonToMap;
import static uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper.extractMessagesFromMap;

@ControllerAdvice
public class CommonExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Convert http status code exception to error response.
     * @should convert HttpStatusCodeException to error response with single message
     * @should convert HttpStatusCodeException to error response with details from body
     */
    @ExceptionHandler
    public ResponseEntity<ApiError> handle(final HttpStatusCodeException hsce, final HttpServletRequest request) {

        ApiError apiError = new ApiError();
        apiError.setTimestamp(Instant.now());
        apiError.setStatus(hsce.getStatusCode().value());
        apiError.setMethod(request.getMethod());
        apiError.setPath(request.getRequestURI());

        List<String> bodyMessages = extractMessagesFromMap(
            convertJsonToMap(hsce.getResponseBodyAsByteArray()), hsce.getStatusCode().value(), hsce.getMessage());
        if (CollectionUtils.isNotEmpty(bodyMessages)) {
            bodyMessages.add(0, hsce.getMessage());
            apiError.setErrors(bodyMessages);
        } else {
            apiError.setErrors(Collections.singletonList(hsce.getMessage()));
        }

        return ResponseEntity.status(hsce.getStatusCode()).body(apiError);

    }

}
