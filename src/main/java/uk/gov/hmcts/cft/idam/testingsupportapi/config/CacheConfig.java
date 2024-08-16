package uk.gov.hmcts.cft.idam.testingsupportapi.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Slf4j
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    @Bean
    public RedisCacheConfiguration cacheConfiguration(@Value("${idam.cache.ttl:1440}") Integer cacheEntryTimeToLive) {
        return RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(cacheEntryTimeToLive))
            .disableCachingNullValues()
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory,
                                     RedisCacheConfiguration defaultConfiguration) {
        return RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(defaultConfiguration).build();
    }


    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("CACHE_FALLBACK: get from cache: {}, key: {}, exception: {}", cache.getName(), key,
                         exception.toString());
                if (exception instanceof RedisConnectionFailureException) {
                    log.error("Failed to connect to redis", exception);
                }
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("CACHE_FALLBACK: put into cache: {}, key: {}, exception: {}", cache.getName(), key,
                         exception.toString());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("CACHE_FALLBACK: evicting from cache: {}, key: {}, exception: {}", cache.getName(), key,
                         exception.toString());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("CACHE_FALLBACK: clearing cache: {}, exception: {}", cache.getName(), exception.toString());
            }
        };
    }

}
