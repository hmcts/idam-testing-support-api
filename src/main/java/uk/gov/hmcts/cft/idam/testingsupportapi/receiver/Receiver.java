package uk.gov.hmcts.cft.idam.testingsupportapi.receiver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.AdminService;

@Slf4j
@Component
public class Receiver {

    private final AdminService adminService;

    public Receiver(AdminService adminService) {
        this.adminService = adminService;
    }

    @JmsListener(destination = "cleanup")
    public void receive(TestingEntity entity) {
        adminService.deleteUser(entity);
    }

}
