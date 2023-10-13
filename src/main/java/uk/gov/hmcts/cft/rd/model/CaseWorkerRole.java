package uk.gov.hmcts.cft.rd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CaseWorkerRole {

    @JsonProperty("role_id")
    String roleId;

    @JsonProperty("role")
    String roleDescription;

    @JsonProperty("is_primary")
    boolean isPrimary;

}
