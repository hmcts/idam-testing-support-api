package uk.gov.hmcts.cft.rpe.api;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@ConditionalOnProperty(value = "featureFlags.s2sTestingSupportEnabled", havingValue = "true", matchIfMissing = false)
@FeignClient(name = "rpetestingsupportapi", url = "${idam.s2s-auth.url}")
public interface RpeS2STestingSupportApi {

    String MICROSERVICE = "microservice";

    default String lease(String serviceName) {
        return lease(Map.of(MICROSERVICE, serviceName));
    }

    @PostMapping("/testing-support/lease")
    String lease(@RequestBody Map<String, String> body);

}
