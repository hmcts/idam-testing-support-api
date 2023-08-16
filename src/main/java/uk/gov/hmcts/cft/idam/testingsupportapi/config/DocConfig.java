package uk.gov.hmcts.cft.idam.testingsupportapi.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
public class DocConfig {

    @Bean
    GroupedOpenApi userApis() {
        return GroupedOpenApi.builder().group("10:user").displayName("users").pathsToMatch("/test/idam/users/**")
            .build();
    }

    @Bean
    GroupedOpenApi notificationApis() {
        return GroupedOpenApi.builder().group("20:notifications").displayName("notifications")
            .pathsToMatch("/test/idam/notifications/**").build();
    }

    @Bean
    GroupedOpenApi serviceProviderApis() {
        return GroupedOpenApi.builder().group("40:services").displayName("services")
            .pathsToMatch("/test/idam/services/**").build();
    }

    @Bean
    GroupedOpenApi roleApis() {
        return GroupedOpenApi.builder().group("30:roles").displayName("roles").pathsToMatch("/test/idam/roles/**")
            .build();
    }

}
