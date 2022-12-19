package uk.gov.hmcts.cft.rd.api.auth;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import uk.gov.hmcts.cft.idam.api.oidc.auth.PasswordGrantRequestInterceptor2;
import uk.gov.hmcts.cft.rpe.api.RpeS2STestingSupportApi;
import uk.gov.hmcts.cft.rpe.api.auth.RpeS2SRequestInterceptor;

import java.util.Map;
import java.util.function.Function;

public class RDAuthConfig2 {

    @Value("${rd.userprofile.api.s2s.servicename}")
    String rdServiceName;

    @Value("${rd.userprofile.client.registration.id}")
    String rdUserProfileClientRegistrationId;

    @Value("${rd.userprofile.client.registration.service-account-user}")
    String rdUserProfileServiceAccountUser;

    @Value("${rd.userprofile.client.registration.service-account-password}")
    String rdUserProfileServiceAccountPassword;

    @Bean
    public RequestInterceptor rdServiceAuthorizationInterceptor(
        RpeS2STestingSupportApi rpeS2STestingSupportApi) {
        return new RpeS2SRequestInterceptor(rpeS2STestingSupportApi, rdServiceName, "/v1/userprofile/.*");
    }

    @Bean
    public OAuth2AuthorizedClientManager oauth2AuthorizedClientManagerPasswordGrant(
        OAuth2AuthorizedClientService oauth2AuthorizedClientService,
        ClientRegistrationRepository clientRegistrationRepository) {
        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
            new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository,
                                                                     oauth2AuthorizedClientService);
        authorizedClientManager
            .setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder().password().refreshToken().build());
        authorizedClientManager.setContextAttributesMapper(systemUserCredentials());
        return authorizedClientManager;
    }

    @Bean
    public RequestInterceptor rdPasswordGrantInterceptor2(
        @Qualifier("oauth2AuthorizedClientManagerPasswordGrant") OAuth2AuthorizedClientManager oauth2AuthorizedClientManager,
        ClientRegistrationRepository clientRegistrationRepository) {
        return new PasswordGrantRequestInterceptor2(
            clientRegistrationRepository.findByRegistrationId(rdUserProfileClientRegistrationId),
            oauth2AuthorizedClientManager,
            "/v1/userprofile/.*");
    }

    private Function<OAuth2AuthorizeRequest, Map<String, Object>> systemUserCredentials() {
        return authorizeRequest -> Map.of(OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME, rdUserProfileServiceAccountUser,
                                      OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME, rdUserProfileServiceAccountPassword);
    }

}
