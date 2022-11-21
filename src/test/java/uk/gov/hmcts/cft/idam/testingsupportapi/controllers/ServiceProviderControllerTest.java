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
import uk.gov.hmcts.cft.idam.api.v2.common.model.ServiceProvider;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingServiceProviderService;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.TestingSessionService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    public void testCreateServiceSuccess() throws Exception {

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
