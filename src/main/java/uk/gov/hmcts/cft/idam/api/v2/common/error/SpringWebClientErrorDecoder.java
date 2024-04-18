package uk.gov.hmcts.cft.idam.api.v2.common.error;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.ArrayList;

import static uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper.exception;

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

        return exception(statusCode, message, responseHeaders, responseBody).orElseGet(() -> delegate.decode(
            methodKey,
            response
        ));

    }
}
