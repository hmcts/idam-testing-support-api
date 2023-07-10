package uk.gov.hmcts.cft.idam.testingsupportapi.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

@Component
@Slf4j
public class SchedulerErrorHandler implements ErrorHandler {
    @Override
    public void handleError(Throwable t) {
        log.warn("scheduler led to exception", t);
    }
}
