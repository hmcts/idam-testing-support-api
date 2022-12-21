package uk.gov.hmcts.cft.rd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserProfile {

    private String userIdentifier;
    private String idamId;
    private UserStatus idamStatus;
    private String email;
    private String firstName;
    private String lastName;
    private UserCategory userCategory;
    private UserType userType;
    private String languagePreference;
    private boolean emailCommsConsent;
    private boolean postalCommsConsent;
    private boolean resendInvite;

    @JsonProperty("roles")
    private List<String> roleNames;

}
