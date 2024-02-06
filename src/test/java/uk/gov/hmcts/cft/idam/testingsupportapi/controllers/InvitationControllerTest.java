package uk.gov.hmcts.cft.idam.testingsupportapi.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.cft.idam.api.v2.common.model.Invitation;
import uk.gov.hmcts.cft.idam.api.v2.common.model.InvitationStatus;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingInvitationService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InvitationController.class)
class InvitationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    TestingInvitationService testingInvitationService;

    @Test
    void getInvitationsByUserEmail() throws Exception {
        Invitation testInvitation = new Invitation();
        testInvitation.setId("test-id");
        testInvitation.setInvitationStatus(InvitationStatus.PENDING);
        when(testingInvitationService.getInvitationsByUserEmail("test@email")).thenReturn(List.of(testInvitation));
        mockMvc.perform(
                get("/test/idam/invitations?email=test@email")
                    .with(jwt()
                              .authorities(new SimpleGrantedAuthority("SCOPE_profile"))
                              .jwt(token -> token.claim("aud", "test-client")
                                  .claim("auditTrackingId", "test-session")
                                  .build())))
            .andExpect(status().isOk())
            .andExpect(content().json("[{'id': 'test-id', 'invitationStatus': 'PENDING'}]"));
    }
}
