package uk.gov.hmcts.cft.idam.testingsupportapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ServiceProvider;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingServiceProviderService;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingSessionService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServiceProviderController.class)
class ServiceProviderControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TestingSessionService testingSessionService;

    @MockBean
    private TestingServiceProviderService testingServiceProviderService;

    @Test
    void testConflictStillDetachesAndReturnsUpstreamDetails() throws Exception {
        TestingSession session = new TestingSession();
        session.setId("session-id");
        when(testingSessionService.getOrCreateSession(any())).thenReturn(session);
        TestingEntity entity = new TestingEntity();
        entity.setId("entity-id");
        when(testingServiceProviderService.findAllActiveByEntityId("test-service-id"))
            .thenReturn(List.of(entity));
        when(testingServiceProviderService.createService(any(), any())).thenThrow(SpringWebClientHelper.exception(
            HttpStatus.CONFLICT, "Conflict", null, """
                {"details":[{"path":"clientId","code":"NOT_UNIQUE","message":"Client already exists"}]}
                """.getBytes(java.nio.charset.StandardCharsets.UTF_8)).orElseThrow());

        mockMvc.perform(post("/test/idam/services")
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_profile")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"clientId\":\"test-service-id\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.details[0].code").value("NOT_UNIQUE"))
            .andExpect(jsonPath("$.details[0].message").value("Client already exists"));
        verify(testingServiceProviderService).detachEntity("entity-id");
    }

    @Test
    void testCreateServiceSuccess() throws Exception {

        ServiceProvider testService = new ServiceProvider();
        testService.setClientId("test-service-id");

        TestingSession testingSession = new TestingSession();
        testingSession.setId(UUID.randomUUID().toString());
        testingSession.setClientId("test-client");
        testingSession.setSessionKey("test-session");

        when(testingSessionService.getOrCreateSession(any())).thenReturn(testingSession);
        when(testingServiceProviderService.createService(any(), any())).thenReturn(testService);

        mockMvc.perform(
            post("/test/idam/services")
                .with(jwt()
                          .authorities(new SimpleGrantedAuthority("SCOPE_profile"))
                          .jwt(token -> token.claim("aud", "test-client")
                              .claim("auditTrackingId", "test-session")
                              .build()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testService)))
            .andExpect(status().isCreated());

    }

}
