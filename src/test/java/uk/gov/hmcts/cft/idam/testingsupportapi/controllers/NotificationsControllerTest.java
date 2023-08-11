package uk.gov.hmcts.cft.idam.testingsupportapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.NotificationsService;
import uk.gov.service.notify.Notification;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = NotificationsController.class)
class NotificationsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationsService notificationsService;

    @Test
    public void testGetLatestNotificationSuccess() throws Exception {
        objectMapper.registerModule(new JodaModule());
        Notification notification = new Notification(buildNotificationContent());
        when(notificationsService.findLatestNotification("test@email")).thenReturn(Optional.of(notification));
        mockMvc.perform(
            get("/test/idam/notifications/latest/test@email")
                .with(jwt()
                          .authorities(new SimpleGrantedAuthority("SCOPE_profile"))
                          .jwt(token -> token.claim("aud", "test-client")
                              .claim("auditTrackingId", "test-session")
                              .build())))
            .andExpect(status().isOk())
            .andExpect(content().json("{'body': 'test-email-body'}"));
    }

    @Test
    public void testGetLatestNotificationNotFound() throws Exception {
        Notification notification = new Notification(buildNotificationContent());
        when(notificationsService.findLatestNotification("test@email")).thenReturn(Optional.empty());
        mockMvc.perform(
            get("/test/idam/notifications/latest/test@email")
                .with(jwt()
                          .authorities(new SimpleGrantedAuthority("SCOPE_profile"))
                          ))
            .andExpect(status().isNotFound())
            .andExpect(content().json("{'path': '/test/idam/notifications/latest/test@email'}"));
    }

    private JSONObject buildNotificationContent() {

        Map<String, Object> template = new HashMap<>();
        template.put("id", UUID.randomUUID().toString());
        template.put("version", 1);
        template.put("uri", "http://test-uri");

        Map<String, Object> map = new HashMap<>();
        map.put("id", UUID.randomUUID().toString());
        map.put("type", "email");
        map.put("body", "test-email-body");
        map.put("status", "sent");
        map.put("created_at", Instant.now().toString());
        map.put("template", template);

        return new JSONObject(map);
    }

}
