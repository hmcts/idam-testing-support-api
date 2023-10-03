package uk.gov.hmcts.cft.idam.testingsupportapi.error;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ApiError;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ErrorDetail;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper.*;

@ControllerAdvice
public class CommonExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Convert http status code exception to error response.
     *
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

        if (StringUtils.startsWith(hsce.getMessage(), ERROR_DETAIL_MARKER)) {
            return handleTrustedException(apiError, hsce);
        }

        List<String> bodyMessages = extractMessagesFromMap(
            convertJsonToMap(hsce.getResponseBodyAsByteArray()),
            hsce.getStatusCode().value(),
            hsce.getMessage()
        );
        if (CollectionUtils.isNotEmpty(bodyMessages)) {
            bodyMessages.add(0, hsce.getMessage());
            apiError.setErrors(bodyMessages);
        } else {
            apiError.setErrors(Collections.singletonList(hsce.getMessage()));
        }

        return ResponseEntity.status(hsce.getStatusCode()).body(apiError);

    }

    private ResponseEntity<ApiError> handleTrustedException(ApiError apiError, HttpStatusCodeException hsce) {
        List<ErrorDetail> details = new ArrayList<>();
        List<String> logDetails = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(hsce.getResponseBodyAsString());
        for (int i = 0; i < jsonArray.length(); i++) {
            ErrorDetail detail = toErrorDetail(jsonArray.getJSONObject(i));
            details.add(detail);
            logDetails.add(detail.getPath() + "/" + detail.getCode() + "/" + detail.getMessage());
        }
        apiError.setDetails(details.isEmpty() ? null : details);

        return ResponseEntity.status(hsce.getStatusCode()).body(apiError);
    }

}
