package uk.gov.hmcts.cft.idam.api.v2.common.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivatedUserRequest {

    private String activationSecretPhrase;
    private User user;

}
