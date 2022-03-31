package uk.gov.hmcts.cft.idam.testingsupportapi.model;

import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;

@Getter
@Setter
public class UserTestingEntity {

    private TestingEntity testingEntity;
    private User user;

}
