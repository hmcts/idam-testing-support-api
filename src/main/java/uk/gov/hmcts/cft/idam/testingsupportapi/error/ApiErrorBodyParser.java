package uk.gov.hmcts.cft.idam.testingsupportapi.error;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ApiError;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ErrorDetail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class ApiErrorBodyParser {

    private static final ObjectReader READER = new ObjectMapper().reader()
        .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

    private ApiErrorBodyParser() {
    }

    static Optional<ApiError> parse(byte[] body) {
        if (body == null || body.length == 0) {
            return Optional.empty();
        }
        try {
            JsonNode root = READER.readTree(body);
            if (root == null || !root.isObject()) {
                return Optional.empty();
            }
            JsonNode errors = root.path("errors");
            JsonNode details = root.path("details");
            if (!isOptionalArray(errors) || !isOptionalArray(details)) {
                return Optional.empty();
            }

            List<String> messages = new ArrayList<>();
            for (JsonNode error : errors) {
                if (!isMessage(error)) {
                    return Optional.empty();
                }
                messages.add(error.textValue());
            }
            List<ErrorDetail> errorDetails = new ArrayList<>();
            for (JsonNode detail : details) {
                if (!detail.isObject() || !isMessage(detail.path("message"))
                    || !isOptionalText(detail.path("path")) || !isOptionalText(detail.path("code"))) {
                    return Optional.empty();
                }
                errorDetails.add(new ErrorDetail(detail.path("path").textValue(),
                                                 detail.path("code").textValue(),
                                                 detail.path("message").textValue()));
            }
            if (messages.isEmpty() && errorDetails.isEmpty()) {
                return Optional.empty();
            }
            ApiError result = new ApiError();
            result.setErrors(messages.isEmpty() ? null : messages);
            result.setDetails(errorDetails.isEmpty() ? null : errorDetails);
            return Optional.of(result);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private static boolean isOptionalArray(JsonNode node) {
        return node.isMissingNode() || node.isNull() || node.isArray();
    }

    private static boolean isOptionalText(JsonNode node) {
        return node.isMissingNode() || node.isNull() || node.isTextual();
    }

    private static boolean isMessage(JsonNode node) {
        return node.isTextual() && !node.textValue().isBlank();
    }
}
