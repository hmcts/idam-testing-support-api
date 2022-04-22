package uk.gov.hmcts.cft.idam.api.v2.common.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.MapUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class SpringWebClientHelper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private SpringWebClientHelper() {
    }

    public static Exception exception(HttpStatus status, Exception e) {
        Optional<Exception> httpException = exception(status,
                                                      e.getClass().getSimpleName() + "; " + e.getMessage(),
                                                      null,
                                                      null
        );
        return httpException.orElse(e);
    }

    public static Optional<Exception> exception(HttpStatus status, String message, HttpHeaders headers, byte[] body) {
        if (status.is4xxClientError()) {
            return Optional
                .of(HttpClientErrorException.create(message, status, status.getReasonPhrase(), headers, body, UTF_8));
        }

        if (status.is5xxServerError()) {
            return Optional
                .of(HttpServerErrorException.create(message, status, status.getReasonPhrase(), headers, body, UTF_8));
        }

        return Optional.empty();
    }

    public static Exception notFound() {
        return HttpClientErrorException
            .create(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), null, null, UTF_8);
    }

    public static Map<String, String> convertJsonToMap(byte[] body) {
        if (body != null) {
            try {
                return objectMapper.readValue(body,
                                              objectMapper.getTypeFactory()
                                                  .constructMapType(HashMap.class, String.class, String.class)
                );
            } catch (IOException e) {
                return Collections.emptyMap();
            }
        }
        return Collections.emptyMap();
    }

    public static List<String> extractMessagesFromMap(Map<String, String> details, Integer statusCode, String message) {
        if (MapUtils.isNotEmpty(details)) {
            List<String> extract = new ArrayList<>();
            for (String key : details.keySet()) {
                if (!"status".equalsIgnoreCase(key)) {
                    String entry = details.get(key);
                    if (!entry.startsWith("" + statusCode) && !entry.equalsIgnoreCase(message)) {
                        extract.add(entry);
                    }
                }
            }
            return extract;
        }
        return Collections.emptyList();
    }

}
