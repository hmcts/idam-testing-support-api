package uk.gov.hmcts.cft.idam.testingsupportapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ActivatedUserRequest;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.model.UserTestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingSessionService;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingUserService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
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

    @Mock
    private Jwt principal;

    @Test
    void testCreateUserSuccess() throws Exception {
        User testUser = new User();
        testUser.setId("1234");
        testUser.setEmail("test@test.local");

        TestingSession testingSession = new TestingSession();
        testingSession.setId(UUID.randomUUID().toString());
        testingSession.setClientId("test-client");
        testingSession.setSessionKey("test-session");

        TestingEntity testingEntity = new TestingEntity();
        testingEntity.setId(UUID.randomUUID().toString());
        testingEntity.setTestingSessionId(testingSession.getId());
        testingEntity.setEntityType(TestingEntityType.USER);
        testingEntity.setEntityId(testUser.getId());

        UserTestingEntity userTestingEntity = new UserTestingEntity();
        userTestingEntity.setUser(testUser);
        userTestingEntity.setTestingEntity(testingEntity);

        when(testingSessionService.getOrCreateSession(eq("test-session"), eq("test-client"))).thenReturn(testingSession);
        when(testingUserService.createTestUser(any(), any(), eq("test-secret"))).thenReturn(userTestingEntity);

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
    void testCreateBurnerUserSuccess() throws Exception {

        User testUser = new User();
        testUser.setId("1234");
        testUser.setEmail("test@test.local");

        UserTestingEntity userTestingEntity = new UserTestingEntity();
        userTestingEntity.setUser(testUser);

        when(testingUserService.createTestUser(isNull(), any(), any())).thenReturn(userTestingEntity);

        ActivatedUserRequest request = new ActivatedUserRequest();
        request.setUser(testUser);
        request.setPassword("test-secret");

        mockMvc.perform(
            post("/test/idam/burner/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

    }
}
