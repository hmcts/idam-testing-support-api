package uk.gov.hmcts.cft.idam.testingsupportapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.AdminService;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingUserService;

import java.time.ZonedDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;

    @MockBean
    private TestingUserService testingUserService;

    @Test
    void triggerExpiryBurnerUsers_success() throws Exception {
        mockMvc.perform(post("/trigger/expiry/burner/users")).andExpect(status().isOk());
        verify(adminService, times(1)).triggerExpiryBurnerUsers();
    }

    @Test
    void triggerExpirySessions_success() throws Exception {
        mockMvc.perform(post("/trigger/expiry/sessions")).andExpect(status().isOk());
        verify(adminService, times(1)).triggerExpirySessions();
    }

    @Test
    void deleteUserTestingEntity() throws Exception {
        TestingEntity testingEntity = new TestingEntity();
        testingEntity.setId("test-id");
        testingEntity.setEntityType(TestingEntityType.USER);
        testingEntity.setTestingSessionId("test-session-id");
        testingEntity.setEntityId("test-entity-id");
        testingEntity.setCreateDate(ZonedDateTime.now());

        mockMvc.perform(
            delete("/admin/entities/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testingEntity)))
            .andExpect(status().isNoContent());

        verify(adminService, times(1)).cleanupUser(any());
    }

    @Test
    void getSessionDependencies() throws Exception {

        TestingEntity testingEntity = new TestingEntity();
        testingEntity.setId("test-id");
        testingEntity.setEntityType(TestingEntityType.USER);
        testingEntity.setTestingSessionId("test-session-id");
        testingEntity.setEntityId("test-entity-id");
        testingEntity.setCreateDate(ZonedDateTime.now());

        when(testingUserService.getTestingEntitiesForSessionById("1234")).thenReturn(List.of(testingEntity));

        mockMvc.perform(get("/admin/sessions/1234/dependencies")
                            .with(jwt()
                                      .authorities(new SimpleGrantedAuthority("SCOPE_profile"))
                                      .jwt(token -> token.claim("aud", "test-client")
                                          .claim("auditTrackingId", "test-session")
                                          .build()))
        ).andExpect(status().isOk());

        verify(testingUserService, times(1)).getTestingEntitiesForSessionById("1234");
    }
}
