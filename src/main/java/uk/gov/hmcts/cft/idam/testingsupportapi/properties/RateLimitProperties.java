package uk.gov.hmcts.cft.idam.testingsupportapi.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.cft.idam.api.v2.common.ratelimit.TokenBucketConfiguration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("idam.ratelimit")
public class RateLimitProperties {

    private TokenBucketConfiguration burnerExpiry;

}
