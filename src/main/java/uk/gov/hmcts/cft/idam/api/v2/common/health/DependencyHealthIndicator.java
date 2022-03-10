package uk.gov.hmcts.cft.idam.api.v2.common.health;

import feign.FeignException;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.cft.idam.api.v2.common.api.HealthApi;

public class DependencyHealthIndicator implements HealthIndicator {

    private final HealthApi healthApi;

    public DependencyHealthIndicator(HealthApi healthApi) {
        this.healthApi = healthApi;
    }

    @Override
    public Health health() {
        try {
            ResponseEntity<String> rsp = healthApi.health();
            if (HttpStatus.OK.equals(rsp.getStatusCode())) {
                return Health.up().build();
            }
            return Health.down().withDetail("status", rsp.getStatusCodeValue()).build();
        } catch (FeignException e) {
            return Health.down().withException(e).build();
        }
    }

}
