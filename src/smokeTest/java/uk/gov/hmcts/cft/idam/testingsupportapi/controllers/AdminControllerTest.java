package uk.gov.hmcts.cft.idam.testingsupportapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.AdminService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

}
