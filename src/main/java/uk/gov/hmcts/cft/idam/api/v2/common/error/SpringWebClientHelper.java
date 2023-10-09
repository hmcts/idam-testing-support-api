package uk.gov.hmcts.cft.idam.api.v2.common.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ErrorDetail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class SpringWebClientHelper {

    public static final String ERROR_DETAIL_MARKER = ">>>";
    private static final String PATH_KEY = "path";
    private static final String CODE_KEY = "code";
    private static final String MESSAGE_KEY = "message";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private SpringWebClientHelper() {
    }

    public static Exception exception(HttpStatus status, Exception e) {
        Optional<Exception> httpException =
            exception(status, e.getClass().getSimpleName() + "; " + e.getMessage(), null, null);
        return httpException.orElse(e);
    }

    public static Optional<Exception> exception(HttpStatus status, String message, HttpHeaders headers, byte[] body) {
        if (status.is4xxClientError()) {
            return Optional.of(HttpClientErrorException.create(message,
                                                               status,
                                                               status.getReasonPhrase(),
                                                               headers,
                                                               body,
                                                               UTF_8
            ));
        }

        if (status.is5xxServerError()) {
            return Optional.of(HttpServerErrorException.create(message,
                                                               status,
                                                               status.getReasonPhrase(),
                                                               headers,
                                                               body,
                                                               UTF_8
            ));
        }

        return Optional.empty();
    }

    public static Exception notFound() {
        return HttpClientErrorException.create(HttpStatus.NOT_FOUND,
                                               HttpStatus.NOT_FOUND.getReasonPhrase(),
                                               null,
                                               null,
                                               UTF_8
        );
    }

    public static Exception conflict() {
        return HttpClientErrorException.create(HttpStatus.CONFLICT,
                                               HttpStatus.CONFLICT.getReasonPhrase(),
                                               null,
                                               null,
                                               UTF_8
        );
    }

    public static Exception conflict(ErrorDetail detail) {
        return createException(HttpStatus.CONFLICT, List.of(detail));
    }

    public static Exception internalServierError() {
        return HttpClientErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR,
                                               HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                                               null,
                                               null,
                                               UTF_8
        );
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

    public static <R> Optional<R> optionalWhenNotFound(Supplier<R> function) {
        try {
            R value = function.get();
            if (value != null) {
                return Optional.of(value);
            }
            return Optional.empty();
        } catch (HttpStatusCodeException hsce) {
            if (hsce.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw hsce;
        }
    }

    public static Exception createException(HttpStatus status, List<ErrorDetail> details) {
        String responseBody = toJsonArray(details);
        String message = getErrorMessage(status, details);
        return exception(status,
                         message,
                         null,
                         responseBody.getBytes()
        ).orElseGet(SpringWebClientHelper::internalServierError);
    }

    private static String getErrorMessage(HttpStatus status, List<ErrorDetail> details) {
        if (CollectionUtils.isNotEmpty(details)) {
            return ERROR_DETAIL_MARKER + "(1/" + details.size() + ") " + details.get(0).getMessage();
        }
        return ERROR_DETAIL_MARKER + status;
    }

    public static String toJsonArray(List<ErrorDetail> details) {
        JSONArray json = new JSONArray();
        for (ErrorDetail detail : details) {
            JSONObject jsonDetail = new JSONObject();
            jsonDetail.put(PATH_KEY, detail.getPath());
            jsonDetail.put(CODE_KEY, detail.getCode());
            jsonDetail.put(MESSAGE_KEY, detail.getMessage());
            json.put(jsonDetail);
        }
        return json.toString();
    }

    public static ErrorDetail toErrorDetail(JSONObject object) {
        return new ErrorDetail(object.getString(PATH_KEY), object.getString(CODE_KEY), object.getString(MESSAGE_KEY));
    }

}
