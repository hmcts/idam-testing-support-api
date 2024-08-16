package uk.gov.hmcts.cft.idam.testingsupportapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.cft.idam.api.v2.common.ratelimit.RateLimitService;
import uk.gov.hmcts.cft.idam.api.v2.common.ratelimit.TokenBucketRepo;
import uk.gov.hmcts.cft.idam.api.v2.common.ratelimit.TokenBucketRepoImpl;
import uk.gov.hmcts.cft.idam.testingsupportapi.properties.RateLimitProperties;

@Configuration
public class RateLimitConfig {

    @Bean
    public RateLimitService burnerExpiryRateLimitService(TokenBucketRepo tokenBucketRepo,
                                                           RateLimitProperties rateLimitProperties) {
        return new RateLimitService(tokenBucketRepo, rateLimitProperties.getBurnerExpiry());
    }

    @Bean
    public TokenBucketRepo tokenBucketRepo() {
        return new TokenBucketRepoImpl();
    }

}
