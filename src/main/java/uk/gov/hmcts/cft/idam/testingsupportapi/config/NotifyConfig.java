package uk.gov.hmcts.cft.idam.testingsupportapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.cft.idam.notify.IdamNotificationClient;

@Configuration
public class NotifyConfig {

    @Bean
    public IdamNotificationClient notificationClient(
        @Value("${notify.key}") String notificationKey,
        ObjectMapper objectMapper) {
        objectMapper.registerModule(new JodaModule());
        return new IdamNotificationClient(notificationKey);
    }

}
