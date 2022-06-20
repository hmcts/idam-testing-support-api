package uk.gov.hmcts.cft.idam.api.v2.common.auth;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

public class IdamClientCredentialsConfig {

    @Value("${idam.client.registration.id}")
    String idamClientRegistrationId;

    @Bean
    public OAuth2AuthorizedClientManager oauth2AuthorizedClientManager(
        OAuth2AuthorizedClientService oauth2AuthorizedClientService,
        ClientRegistrationRepository clientRegistrationRepository) {
        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
            new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository,
                                                                     oauth2AuthorizedClientService);
        authorizedClientManager
            .setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build());
        return authorizedClientManager;
    }

    @Bean
    public RequestInterceptor idamAuthenticationRequestInterceptor(
        OAuth2AuthorizedClientManager oauth2AuthorizedClientManager,
        ClientRegistrationRepository clientRegistrationRepository) {
        return new ClientCredentialsRequestInterceptor(
            clientRegistrationRepository.findByRegistrationId(idamClientRegistrationId),
            oauth2AuthorizedClientManager,
            "/api/v2/.*"
        );
    }

}
