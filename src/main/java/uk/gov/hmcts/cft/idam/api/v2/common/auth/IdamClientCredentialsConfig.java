package uk.gov.hmcts.cft.idam.api.v2.common.auth;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import uk.gov.hmcts.cft.idam.api.v2.oidc.auth.ClientCredentialsRequestInterceptor;

public class IdamClientCredentialsConfig {

    @Value("${idam.client.registration.id}")
    String idamClientRegistrationId;

    @Bean
    public RequestInterceptor idamAuthenticationRequestInterceptor(
        OAuth2AuthorizedClientManager oauth2AuthorizedClientManager,
        ClientRegistrationRepository clientRegistrationRepository) {
        return new ClientCredentialsRequestInterceptor(
            clientRegistrationRepository.findByRegistrationId(idamClientRegistrationId),
            oauth2AuthorizedClientManager,
            "(/api/v2/.*|/api/v1/staleUsers/.*)"
        );
    }

}
