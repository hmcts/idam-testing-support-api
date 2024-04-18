package uk.gov.hmcts.cft.rd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateCaseWorkerProfileRequest extends CaseWorkerProfile {

    List<CaseWorkerService> services;
    List<CaseWorkerRole> roles;
    List<String> skills;

    @JsonProperty("base_locations")
    List<CaseWorkerLocation> baseLocations;

    String region;

    @JsonProperty("region_id")
    String regionId;

    @JsonProperty("user_type")
    String userType;

    @JsonProperty("up_idam_status")
    String userProfileIdamStatus;

    @JsonProperty("task_supervisor")
    boolean taskSupervisor;

    @JsonProperty("case_allocator")
    boolean caseAllocator;

    @JsonProperty("staff_admin")
    boolean staffAdmin;

}
