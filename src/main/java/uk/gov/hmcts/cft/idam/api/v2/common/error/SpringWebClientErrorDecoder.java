package uk.gov.hmcts.cft.idam.api.v2.common.error;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ErrorDetail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper.createException;
import static uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper.exception;
import static uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper.toErrorDetail;

/**
 * https://github.com/spring-cloud/spring-cloud-openfeign/issues/118
 */
@Slf4j
public class SpringWebClientErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder delegate = new ErrorDecoder.Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpHeaders responseHeaders = new HttpHeaders();
        response.headers().forEach((key, value) -> responseHeaders.put(key, new ArrayList<>(value)));

        HttpStatus statusCode = HttpStatus.valueOf(response.status());
        String message = response.reason();

        byte[] responseBody;
        try {
            if (response.body() != null) {
                responseBody = IOUtils.toByteArray(response.body().asInputStream());
            } else {
                responseBody = "".getBytes();
            }
        } catch (IOException e) {
            responseBody = "invalid response body".getBytes();
            log.error("Failed to process response body.", e);
        }

        if ((statusCode.is4xxClientError() || statusCode.is5xxServerError())
            && (methodKey.startsWith("IdamV2UserManagementApi#")
                || methodKey.startsWith("IdamV2ConfigApi#")
                || methodKey.startsWith("IdamV2InvitationApi#"))) {
            List<ErrorDetail> details = readDetails(responseBody);
            if (!details.isEmpty()) {
                return createException(statusCode, details);
            }
        }

        return exception(statusCode, message, responseHeaders, responseBody).orElseGet(() -> delegate.decode(
            methodKey,
            response
        ));

    }

    private List<ErrorDetail> readDetails(byte[] body) {
        try {
            JSONTokener tokener = new JSONTokener(new String(body, UTF_8));
            JSONObject error = new JSONObject(tokener);
            JSONArray array = error.optJSONArray("details");
            if (array == null || tokener.nextClean() != 0) {
                return List.of();
            }
            List<ErrorDetail> details = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                details.add(toErrorDetail(array.getJSONObject(i)));
            }
            return details;
        } catch (JSONException e) {
            return List.of();
        }
    }
}
