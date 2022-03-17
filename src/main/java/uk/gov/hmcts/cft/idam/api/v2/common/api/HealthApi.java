package uk.gov.hmcts.cft.idam.api.v2.common.api;


import org.springframework.web.bind.annotation.GetMapping;

public interface HealthApi {

    @GetMapping("/health")
    String health();

}
