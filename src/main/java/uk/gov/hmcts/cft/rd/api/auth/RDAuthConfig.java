package uk.gov.hmcts.cft.rd.api.auth;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import uk.gov.hmcts.cft.idam.api.oidc.auth.PasswordGrantRequestInterceptor;
import uk.gov.hmcts.cft.rpe.api.RpeS2STestingSupportApi;
import uk.gov.hmcts.cft.rpe.api.auth.RpeS2SRequestInterceptor;

public class RDAuthConfig {

    @Value("${rd.userprofile.api.s2s.servicename}")
    String rdServiceName;

    @Value("${rd.userprofile.client.registration.id}")
    String rdUserProfileClientRegistrationId;

    @Value("${rd.userprofile.client.registration.service-account-user}")
    String rdUserProfileServiceAccountUser;

    @Value("${rd.userprofile.client.registration.service-account-password}")
    String rdUserProfileServiceAccountPassword;

    @Bean
    public RequestInterceptor rdServiceAuthorizationInterceptor(RpeS2STestingSupportApi rpeS2STestingSupportApi) {
        return new RpeS2SRequestInterceptor(rpeS2STestingSupportApi, rdServiceName, "/v1/userprofile.*");
    }

    @Bean
    public RequestInterceptor rdPasswordGrantInterceptor(OAuth2AuthorizedClientManager oauth2AuthorizedClientManager,
                                                          ClientRegistrationRepository clientRegistrationRepository) {
        return new PasswordGrantRequestInterceptor(clientRegistrationRepository
                                                       .findByRegistrationId(rdUserProfileClientRegistrationId),
                                                   oauth2AuthorizedClientManager,
                                                   rdUserProfileServiceAccountUser,
                                                   rdUserProfileServiceAccountPassword,
                                                   "/v1/userprofile.*"
        );
    }

}
