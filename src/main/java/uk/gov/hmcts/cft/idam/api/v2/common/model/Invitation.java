package uk.gov.hmcts.cft.idam.api.v2.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
public class Invitation {

    private String id;

    private InvitationType invitationType;
    private InvitationStatus invitationStatus;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String activationToken;

    private String userId;

    @NotEmpty
    private String email;

    private String forename;
    private String surname;
    private List<String> activationRoleNames;
    private String clientId;
    private String successRedirect;
    private String invitedBy;
    private ZonedDateTime createDate;
    private ZonedDateTime lastModified;

}
