package uk.gov.hmcts.cft.idam.api.v2.common.ratelimit;

import com.google.common.annotations.VisibleForTesting;

import java.time.Clock;
import java.time.ZoneOffset;

public class RateLimitService {

    public enum RateLimitServiceOutcome {
        TOO_MANY_REQUESTS, REQUEST_CAPTURED
    }

    private final TokenBucketRepo tokenBucketRepo;
    private final TokenBucketConfiguration tokenBucketConfiguration;

    private Clock clock;

    public RateLimitService(TokenBucketRepo tokenBucketRepo, TokenBucketConfiguration tokenBucketConfiguration) {
        this.tokenBucketRepo = tokenBucketRepo;
        this.tokenBucketConfiguration = tokenBucketConfiguration;
        this.clock = Clock.system(ZoneOffset.UTC);
    }

    @VisibleForTesting
    void changeClock(Clock clock) {
        this.clock = clock;
    }

    /**
     * @should return request captured if bucket did not exist
     * @should return request captured if bucket exists and has tokens
     * @should return request captured if bucket exists and was refilled
     * @should return too many requests if bucket does not contain enough tokens
     * @should return request captured if disabled
     */
    public RateLimitServiceOutcome rateLimitByBucket(String requestKey) {
        if (!tokenBucketConfiguration.isEnabled()) {
            return RateLimitServiceOutcome.REQUEST_CAPTURED;
        }
        String cacheKey = tokenBucketConfiguration.getPrefix() + requestKey;
        Long requestTimestamp = clock.millis();
        TokenBucket bucket = tokenBucketRepo.getTokenBucket(cacheKey);
        if (bucket == null) {
            bucket = buildTokenBucket(requestTimestamp);
        } else {
            refillBucket(bucket, requestTimestamp);
        }
        boolean hasToken = false;
        if (bucket.getTokenCount() > 0) {
            hasToken = true;
            bucket.setTokenCount(bucket.getTokenCount() - 1);
        }
        bucket.setLatestRequestTS(requestTimestamp);
        tokenBucketRepo.updateTokenBucket(cacheKey, bucket);
        if (hasToken) {
            return RateLimitServiceOutcome.REQUEST_CAPTURED;
        }
        return RateLimitServiceOutcome.TOO_MANY_REQUESTS;
    }

    private void refillBucket(TokenBucket bucket, Long requestTimestamp) {
        int credits = calculateCredits(bucket.getPreviousRefillTS(), requestTimestamp);
        if (credits > 0) {
            int balance = Math.min(bucket.getTokenCount() + credits, tokenBucketConfiguration.getTokenLimit());
            bucket.setTokenCount(balance);
            bucket.setPreviousRefillTS(requestTimestamp);
        }
    }

    /**
     * @should return 0 if there are no credits in the time period
     * @should return credits for time period
     */
    protected int calculateCredits(Long previousRefillTS, Long nowTS) {
        return (int) (
            ((nowTS - previousRefillTS) / tokenBucketConfiguration.getTokenRefillDuration().toMillis())
                * tokenBucketConfiguration.getTokenRefillAmount());
    }

    private TokenBucket buildTokenBucket(Long timestamp) {
        TokenBucket tokenBucket = new TokenBucket();
        tokenBucket.setTokenCount(tokenBucketConfiguration.getTokenLimit());
        tokenBucket.setPreviousRefillTS(timestamp);
        return tokenBucket;
    }

}
