package uk.gov.hmcts.cft.idam.api.v2.common.error;

import feign.RetryableException;
import feign.Retryer;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;

import static uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper.exception;

public class SpringWebClientRetryer implements Retryer {

    private final Retryer delegate;

    public SpringWebClientRetryer() {
        delegate = new Retryer.Default();
    }

    public SpringWebClientRetryer(long period, long maxPeriod, int maxAttempts) {
        delegate = new Retryer.Default(period, maxPeriod, maxAttempts);
    }

    @SneakyThrows
    @Override
    public void continueOrPropagate(RetryableException e) {
        try {
            delegate.continueOrPropagate(e);
        } catch (RetryableException re) {
            HttpHeaders responseHeaders = new HttpHeaders();
            re.responseHeaders().forEach((key, value) -> responseHeaders.put(key, new ArrayList<>(value)));
            HttpStatus statusCode = HttpStatus.resolve(re.status());
            if (statusCode == null) {
                statusCode = HttpStatus.BAD_GATEWAY;
            }

            byte[] responseBody;
            if (re.responseBody().isPresent()) {
                responseBody = new byte[re.responseBody().get().remaining()];
                re.responseBody().get().get(responseBody, 0, responseBody.length);
            } else {
                responseBody = new byte[]{};
            }

            throw exception(statusCode, re.getMessage(), responseHeaders, responseBody)
                .orElse(re);

        }
    }

    @Override
    public Retryer clone() {
        return new SpringWebClientRetryer();
    }

}
