package uk.gov.hmcts.cft.idam.testingsupportapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ActivatedUserRequest;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingSessionService;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingUserService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TestingSessionService testingSessionService;

    @MockBean
    private TestingUserService testingUserService;

    @Test
    void testCreateUserSuccess() throws Exception {
        User testUser = new User();
        testUser.setId("1234");
        testUser.setEmail("test@test.local");

        TestingSession testingSession = new TestingSession();
        testingSession.setId(UUID.randomUUID().toString());
        testingSession.setClientId("test-client");
        testingSession.setSessionKey("test-session");

        when(testingSessionService.getOrCreateSession(any())).thenReturn(testingSession);
        when(testingUserService.createTestUser(any(), any(), eq("test-secret"))).thenReturn(testUser);

        ActivatedUserRequest request = new ActivatedUserRequest();
        request.setUser(testUser);
        request.setPassword("test-secret");

        mockMvc.perform(
            post("/test/idam/users")
                .with(jwt()
                    .authorities(new SimpleGrantedAuthority("SCOPE_profile"))
                          .jwt(token -> token.claim("aud", "test-client")
                              .claim("auditTrackingId", "test-session")
                              .build()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    void testRemoveUserSuccess() throws Exception {
        TestingSession testingSession = new TestingSession();
        testingSession.setId(UUID.randomUUID().toString());
        testingSession.setClientId("test-client");
        testingSession.setSessionKey("test-session");

        when(testingSessionService.getOrCreateSession(any())).thenReturn(testingSession);

        mockMvc.perform(
            delete("/test/idam/users/test-user-id")
                .with(jwt()
                          .authorities(new SimpleGrantedAuthority("SCOPE_profile"))
                          .jwt(token -> token.claim("aud", "test-client")
                              .claim("auditTrackingId", "test-session")
                              .build())))
        .andExpect(status().isNoContent());

        verify(testingUserService).addTestEntityToSessionForRemoval(testingSession, "test-user-id");
    }

    @Test
    void testCreateBurnerUserSuccess() throws Exception {

        User testUser = new User();
        testUser.setId("1234");
        testUser.setEmail("test@test.local");

        when(testingUserService.createTestUser(isNull(), any(), any())).thenReturn(testUser);

        ActivatedUserRequest request = new ActivatedUserRequest();
        request.setUser(testUser);
        request.setPassword("test-secret");

        mockMvc.perform(
            post("/test/idam/burner/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_profile"))))
            .andExpect(status().isCreated());

    }

    @Test
    void testRemoveBurnerUserForceSuccess() throws Exception {
        mockMvc.perform(
            delete("/test/idam/burner/users/test-user-id")
                .header("force", "true")
                .with(jwt()
                          .authorities(new SimpleGrantedAuthority("SCOPE_profile"))
                          .jwt(token -> token.claim("aud", "test-client")
                              .claim("auditTrackingId", "test-session")
                              .build())))
            .andExpect(status().isNoContent());

        verify(testingUserService).forceRemoveTestUser("test-user-id");
    }

    @Test
    void testRemoveBurnerUserSuccess() throws Exception {
        mockMvc.perform(
            delete("/test/idam/burner/users/test-user-id")
                .with(jwt()
                          .authorities(new SimpleGrantedAuthority("SCOPE_profile"))
                          .jwt(token -> token.claim("aud", "test-client")
                              .claim("auditTrackingId", "test-session")
                              .build())))
            .andExpect(status().isNoContent());

        verify(testingUserService).removeTestUser("test-user-id");
    }

    @Test
    void testGetUser() throws Exception {
        mockMvc.perform(
          get("/test/idam/users/1234")
              .with(jwt()
                        .authorities(new SimpleGrantedAuthority("SCOPE_profile"))
                        .jwt(token -> token.claim("aud", "test-client")
                            .claim("auditTrackingId", "test-session")
                            .build())))
            .andExpect(status().isOk());

        verify(testingUserService).getUserByUserId("1234");
    }

    @Test
    void testGetUserByEmail() throws Exception {
        mockMvc.perform(
                get("/test/idam/users?email=test@local")
                    .with(jwt()
                              .authorities(new SimpleGrantedAuthority("SCOPE_profile"))
                              .jwt(token -> token.claim("aud", "test-client")
                                  .claim("auditTrackingId", "test-session")
                                  .build())))
            .andExpect(status().isOk());

        verify(testingUserService).getUserByEmail("test@local");
    }
}
