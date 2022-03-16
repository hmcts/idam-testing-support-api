package uk.gov.hmcts.cft.idam.api.v2.common.error;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SpringWebClientHelper {

    public static Optional<Exception> exception(HttpStatus status, String message, HttpHeaders headers, byte[] body) {
        if (status.is4xxClientError()) {
            return Optional.of(HttpClientErrorException.create(message, status, status.getReasonPhrase(), headers, body, UTF_8));
        }

        if (status.is5xxServerError()) {
            return Optional.of(HttpServerErrorException.create(message, status, status.getReasonPhrase(), headers, body, UTF_8));
        }

        return Optional.empty();
    }

}
