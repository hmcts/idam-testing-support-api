package uk.gov.hmcts.cft.idam.testingsupportapi.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

@Component
@Slf4j
public class ListenerErrorHandler implements ErrorHandler {
    @Override
    public void handleError(Throwable t) {
        log.warn("Listener led to exception", t);
    }

}
