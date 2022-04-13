package uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model;

import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;

@Getter
@Setter
public class CleanupEntity {

    private String testingEntityId;
    private String testingSessionId;
    private String entityId;
    private TestingEntityType testingEntityType;
    private String clientId;

}
