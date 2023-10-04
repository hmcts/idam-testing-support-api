package uk.gov.hmcts.cft.rd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CaseWorkerProfile {

    @JsonProperty("case_worker_id")
    private String caseWorkerId;

    @JsonProperty("email_id")
    private String email;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private boolean suspended;

}
