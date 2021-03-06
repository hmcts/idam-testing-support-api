package uk.gov.hmcts.cft.idam.testingsupportapi.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
)
public class DocConfig {

    @Bean
    GroupedOpenApi userApis() {
        return GroupedOpenApi.builder().group("user").pathsToMatch("/test/idam/users/**").build();
    }

    @Bean
    GroupedOpenApi burnerApis() {
        return GroupedOpenApi.builder().group("burner").pathsToMatch("/test/idam/burner/users/**").build();
    }

    @Bean
    GroupedOpenApi notificationApis() {
        return GroupedOpenApi.builder().group("notifications").pathsToMatch("/test/idam/notifications/**").build();
    }

}
