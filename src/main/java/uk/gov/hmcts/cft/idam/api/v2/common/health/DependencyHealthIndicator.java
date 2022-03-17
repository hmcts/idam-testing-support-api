package uk.gov.hmcts.cft.idam.api.v2.common.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.web.client.HttpStatusCodeException;
import uk.gov.hmcts.cft.idam.api.v2.common.api.HealthApi;

public class DependencyHealthIndicator implements HealthIndicator {

    private final HealthApi healthApi;

    public DependencyHealthIndicator(HealthApi healthApi) {
        this.healthApi = healthApi;
    }

    @Override
    public Health health() {
        try {
            healthApi.health();
            return Health.up().build();
        } catch (HttpStatusCodeException hse) {
            return Health.down().withException(hse).build();
        }
    }

}
