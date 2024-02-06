package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cft.idam.api.v2.IdamV2InvitationApi;
import uk.gov.hmcts.cft.idam.api.v2.common.model.Invitation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestingInvitationServiceTest {

    @Mock
    IdamV2InvitationApi idamV2InvitationApi;

    @InjectMocks
    TestingInvitationService underTest;

    @Test
    void getInvitationsByUserEmail() {
        Invitation testInvitation = new Invitation();
        when(idamV2InvitationApi.getInvitationsByUserEmail("test@test")).thenReturn(List.of(testInvitation));
        List<Invitation> result = underTest.getInvitationsByUserEmail("test@test");
        assertEquals(1, result.size());
        assertEquals(testInvitation, result.get(0));
    }
}
