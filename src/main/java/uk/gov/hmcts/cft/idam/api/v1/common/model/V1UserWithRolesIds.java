package uk.gov.hmcts.cft.idam.api.v1.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
public class V1UserWithRolesIds {

    private String id;
    private String email;
    private String forename;
    private String surname;
    private String ssoId;
    private String ssoProvider;

    @JsonProperty("roles")
    private List<String> roleIds;

    private boolean active;
    private boolean locked;
    private boolean pending;
    private boolean stale;

    private ZonedDateTime createDate;
    private ZonedDateTime lastModified;
    private ZonedDateTime pwdAccountLockedTime;

}
