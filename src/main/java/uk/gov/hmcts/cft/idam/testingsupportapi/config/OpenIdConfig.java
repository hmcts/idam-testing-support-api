package uk.gov.hmcts.cft.idam.testingsupportapi.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

@Configuration public class OpenIdConfig {

    @Bean public OAuth2AuthorizedClientManager oauth2AuthorizedClientManager(
        OAuth2AuthorizedClientService oauth2AuthorizedClientService,
        ClientRegistrationRepository clientRegistrationRepository) {
        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
            new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository,
                                                                     oauth2AuthorizedClientService
            );
        authorizedClientManager.setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder()
                                                                .clientCredentials().password().refreshToken().build());
        authorizedClientManager.setContextAttributesMapper(systemUserCredentials());
        return authorizedClientManager;
    }

    private Function<OAuth2AuthorizeRequest, Map<String, Object>> systemUserCredentials() {
        return authorizeRequest -> {
            String username = authorizeRequest.getAttribute(OAuth2ParameterNames.USERNAME);
            String password = authorizeRequest.getAttribute(OAuth2ParameterNames.PASSWORD);
            if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
                return Map.of(OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME,
                              username,
                              OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME,
                              password
                );
            }
            return Collections.emptyMap();
        };
    }

}
