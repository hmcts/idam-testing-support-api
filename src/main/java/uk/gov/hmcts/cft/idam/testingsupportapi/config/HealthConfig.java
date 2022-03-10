package uk.gov.hmcts.cft.idam.testingsupportapi.config;

import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.cft.idam.api.v2.common.api.IdamApiHealth;
import uk.gov.hmcts.cft.idam.api.v2.common.health.DependencyHealthIndicator;

@Configuration
public class HealthConfig {

    @Bean
    public HealthIndicator idamApiHealthIndicator(IdamApiHealth healthClient) {
        return new DependencyHealthIndicator(healthClient);
    }

}
