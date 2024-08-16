package uk.gov.hmcts.cft.idam.api.v2.common.ratelimit;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenBucket {

    private int tokenCount;
    private Long previousRefillTS;
    private Long latestRequestTS;

}
