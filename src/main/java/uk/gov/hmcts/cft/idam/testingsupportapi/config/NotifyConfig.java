package uk.gov.hmcts.cft.idam.testingsupportapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.service.notify.NotificationClient;

@Configuration
public class NotifyConfig {

    @Bean
    public NotificationClient notificationClient(@Value("${notify.key}") String notificationKey) {
        return new NotificationClient(notificationKey);
    }

}
