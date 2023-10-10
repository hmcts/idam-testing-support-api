package uk.gov.hmcts.cft.idam.api.v2.common.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Role {

    @NotNull
    private String name;

    private String id;
    private String description;
    private List<String> assignableRoleNames;

}
