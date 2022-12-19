package uk.gov.hmcts.cft.rd.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class UserProfile {

    private String id;
    private String idamId;
    private String idamStatus;
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
