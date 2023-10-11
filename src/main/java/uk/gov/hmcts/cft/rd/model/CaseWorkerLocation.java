package uk.gov.hmcts.cft.rd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CaseWorkerLocation {

    @JsonProperty("location_id")
    String locationId;

    @JsonProperty("location")
    String locationDescription;

    @JsonProperty("is_primary")
    boolean isPrimary;

}
