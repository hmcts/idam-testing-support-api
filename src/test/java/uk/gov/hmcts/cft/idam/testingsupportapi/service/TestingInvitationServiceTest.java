package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cft.idam.api.v2.IdamV2InvitationApi;
import uk.gov.hmcts.cft.idam.api.v2.common.model.Invitation;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestingInvitationServiceTest {

    @Mock
    IdamV2InvitationApi idamV2InvitationApi;

    @Mock
    TestingEntityRepo testingEntityRepo;

    @InjectMocks
    TestingInvitationService underTest;

    @Captor
    ArgumentCaptor<TestingEntity> testingEntityArgumentCaptor;

    @Test
    void getInvitationsByUserEmail() {
        Invitation testInvitation = new Invitation();
        when(idamV2InvitationApi.getInvitationsByUserEmail("test@test")).thenReturn(List.of(testInvitation));
        List<Invitation> result = underTest.getInvitationsByUserEmail("test@test");
        assertEquals(1, result.size());
        assertEquals(testInvitation, result.get(0));
    }

    @Test
    void createInvitation_shouldCreateInvitationAndTestingEntity() {
        Invitation invitation = new Invitation();
        invitation.setId("test-invitation-id");
        when(idamV2InvitationApi.createInvitation(invitation)).thenReturn(invitation);
        when(testingEntityRepo.save(any())).then(returnsFirstArg());

        String sessionId = UUID.randomUUID().toString();
        Invitation result = underTest.createTestInvitation(sessionId, invitation);
        assertEquals(invitation, result);

        verify(testingEntityRepo, times(1)).save(testingEntityArgumentCaptor.capture());

        TestingEntity testingEntity = testingEntityArgumentCaptor.getValue();

        assertEquals("test-invitation-id", testingEntity.getEntityId());
        assertEquals(sessionId, testingEntity.getTestingSessionId());
        assertEquals(TestingEntityType.INVITATION, testingEntity.getEntityType());
        assertNotNull(testingEntity.getCreateDate());
    }

    @Test
    void deleteEntity() {
        underTest.deleteEntity("test-entity-id");
        verify(idamV2InvitationApi.revokeInvitation("test-entity-id"));
    }
}
