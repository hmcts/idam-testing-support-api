package uk.gov.hmcts.cft.rpe.api;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "rpetestingsupportapi", url = "${rpe.auth.api.url}")
public interface RpeS2STestingSupportApi {

    String MICROSERVICE = "microservice";

    @Cacheable("tokencaches2s")
    default String lease(String serviceName) {
        return lease(Map.of(MICROSERVICE, serviceName));
    }

    @PostMapping("/testing-support/lease")
    String lease(@RequestBody Map<String, String> body);

}
