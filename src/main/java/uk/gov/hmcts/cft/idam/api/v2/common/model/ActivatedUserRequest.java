package uk.gov.hmcts.cft.idam.api.v2.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivatedUserRequest {

    private String password;
    private User user;

    @JsonProperty("activationSecretPhrase")
    void setActivationSecretPhrase(String phrase) {
        this.password = phrase;
    }

}
