package uk.gov.hmcts.cft.idam.api.v2.common.api;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name="idamapihealth", url="${idam.api.url}")
public interface IdamApiHealth extends HealthApi {
}
