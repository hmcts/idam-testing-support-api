package uk.gov.hmcts.cft.idam.testingsupportapi.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

@ExtendWith(MockitoExtension.class)
public class SleepHelperTest {

    @Test
    public void testZeroDuration() {
        SleepHelper.safeSleep(Duration.ZERO);
        assert(true);
    }

    @Test
    public void testPositiveDuration() {
        SleepHelper.safeSleep(Duration.ofMillis(1));
        assert(true);
    }

}
