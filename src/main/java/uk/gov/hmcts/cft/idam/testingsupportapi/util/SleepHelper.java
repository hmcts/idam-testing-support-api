package uk.gov.hmcts.cft.idam.testingsupportapi.util;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
public class SleepHelper {

    private SleepHelper() {
    }

    public static void safeSleep(Duration duration) {
        if (!duration.isZero()) {
            try {
                Thread.sleep(duration);
            } catch (InterruptedException e) {
                log.warn("Failed to sleep after creation of service", e);
            }
        }
    }

}
