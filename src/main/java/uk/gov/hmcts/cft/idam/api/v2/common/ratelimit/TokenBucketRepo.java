package uk.gov.hmcts.cft.idam.api.v2.common.ratelimit;

public interface TokenBucketRepo {

    TokenBucket getTokenBucket(String bucketKey);

    TokenBucket updateTokenBucket(String bucketKey, TokenBucket bucket);

}
