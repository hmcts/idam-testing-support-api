package uk.gov.hmcts.cft.idam.testingsupportapi.config;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocConfig {

    @Bean
    GroupedOpenApi apis() {
        return GroupedOpenApi.builder().group("all").pathsToMatch("/**").build();
    }

}
