package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cft.idam.api.v2.IdamV2InvitationApi;
import uk.gov.hmcts.cft.idam.api.v2.common.model.Invitation;

import java.util.List;

@Slf4j
@Service
public class TestingInvitationService {

    private final IdamV2InvitationApi idamV2InvitationApi;

    public TestingInvitationService(IdamV2InvitationApi idamV2InvitationApi) {
        this.idamV2InvitationApi = idamV2InvitationApi;
    }

    public List<Invitation> getInvitationsByUserEmail(String email) {
        return idamV2InvitationApi.getInvitationsByUserEmail(email);
    }

}
