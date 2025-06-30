package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cft.idam.api.v2.IdamV2InvitationApi;
import uk.gov.hmcts.cft.idam.api.v2.common.model.Invitation;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;

import java.util.List;

@Slf4j
@Service
public class TestingInvitationService extends TestingEntityService<Invitation> {

    private final IdamV2InvitationApi idamV2InvitationApi;

    protected TestingInvitationService(TestingEntityRepo testingEntityRepo,
                                       JmsTemplate jmsTemplate, IdamV2InvitationApi idamV2InvitationApi) {
        super(testingEntityRepo, jmsTemplate);
        this.idamV2InvitationApi = idamV2InvitationApi;
    }


    public List<Invitation> getInvitationsByUserEmail(String email) {
        return idamV2InvitationApi.getInvitationsByUserEmail(email);
    }

    public Invitation createTestInvitation(String sessionId, Invitation invitation) {
        Invitation testInvitation = idamV2InvitationApi.createInvitation(invitation);
        createTestingEntity(sessionId, testInvitation);
        return testInvitation;
    }

    @Override
    protected void deleteEntity(String key) {
        idamV2InvitationApi.revokeInvitation(key);
    }

    @Override
    protected String getEntityKey(Invitation entity) {
        return entity.getId();
    }

    @Override
    protected TestingEntityType getTestingEntityType() {
        return TestingEntityType.INVITATION;
    }

}
