package uk.gov.hmcts.cft.rd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CaseWorkerService {

    @JsonProperty("service")
    String serviceDescription;

    @JsonProperty("service_code")
    String serviceCode;

}
