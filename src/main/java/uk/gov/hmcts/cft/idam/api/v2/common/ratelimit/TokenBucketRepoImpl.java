package uk.gov.hmcts.cft.idam.api.v2.common.ratelimit;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

@Repository
public class TokenBucketRepoImpl implements TokenBucketRepo {

    @Cacheable(value = "tokenBucketCache", key = "#bucketKey", unless = "#result==null")
    @Override
    public TokenBucket getTokenBucket(String bucketKey) {
        return null;
    }

    @CachePut(value = "tokenBucketCache", key = "#bucketKey", unless = "#result==null")
    @Override
    public TokenBucket updateTokenBucket(String bucketKey, TokenBucket bucket) {
        return bucket;
    }

}
