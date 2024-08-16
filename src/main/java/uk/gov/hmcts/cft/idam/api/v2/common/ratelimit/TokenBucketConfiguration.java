package uk.gov.hmcts.cft.idam.api.v2.common.ratelimit;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@Getter
@Setter
public class TokenBucketConfiguration {

    private boolean enabled;
    private String prefix;
    private int tokenLimit;
    private int tokenRefillAmount;
    private Duration tokenRefillDuration;

}
