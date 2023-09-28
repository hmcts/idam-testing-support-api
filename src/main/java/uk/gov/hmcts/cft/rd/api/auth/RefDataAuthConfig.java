package uk.gov.hmcts.cft.rd.api.auth;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import uk.gov.hmcts.cft.idam.api.v2.oidc.auth.PasswordGrantRequestInterceptor;
import uk.gov.hmcts.cft.rpe.api.RpeS2STestingSupportApi;
import uk.gov.hmcts.cft.rpe.api.auth.RpeS2SRequestInterceptor;
import uk.gov.hmcts.cft.rpe.api.auth.RpeS2STestingSupportAuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;
import uk.gov.hmcts.reform.authorisation.generators.AutorefreshingJwtAuthTokenGenerator;

public class RefDataAuthConfig {

    @Value("${idam.s2s-auth.microservice}")
    String rdServiceName;

    @Value("${idam.s2s-auth.totp_secret}")
    String rdServiceSecret;

    @Value("${rd.userprofile.client.registration.id}")
    String rdUserProfileClientRegistrationId;

    @Value("${rd.userprofile.client.registration.service-account-user}")
    String rdUserProfileServiceAccountUser;

    @Value("${rd.userprofile.client.registration.service-account-password}")
    String rdUserProfileServiceAccountPassword;

    @ConditionalOnProperty(value = "featureFlags.s2sTestingSupportEnabled", havingValue = "false",
        matchIfMissing = true)
    @Primary
    @Bean
    public AuthTokenGenerator s2sAuthTokenGenerator(ServiceAuthorisationApi serviceAuthorisationApi) {
        return AuthTokenGeneratorFactory.createDefaultGenerator(
            rdServiceSecret,
            rdServiceName,
            serviceAuthorisationApi
        );
    }

    @ConditionalOnProperty(value = "featureFlags.s2sTestingSupportEnabled", havingValue = "true",
        matchIfMissing = false)
    @Bean
    public AuthTokenGenerator s2sTestingSupportAuthTokenGenerator(RpeS2STestingSupportApi rpeS2STestingSupportApi) {
        AuthTokenGenerator atg = new RpeS2STestingSupportAuthTokenGenerator(rdServiceName, rpeS2STestingSupportApi);
        return new AutorefreshingJwtAuthTokenGenerator(atg);
    }

    @Bean
    public RequestInterceptor rdServiceAuthorizationInterceptor(AuthTokenGenerator authTokenGenerator) {
        return new RpeS2SRequestInterceptor(authTokenGenerator, "(/v1/userprofile|/refdata/).*");
    }

    @Bean
    public RequestInterceptor rdPasswordGrantInterceptor(OAuth2AuthorizedClientManager oauth2AuthorizedClientManager,
                                                         ClientRegistrationRepository clientRegistrationRepository) {
        return new PasswordGrantRequestInterceptor(
            clientRegistrationRepository.findByRegistrationId(rdUserProfileClientRegistrationId),
            oauth2AuthorizedClientManager,
            rdUserProfileServiceAccountUser,
            rdUserProfileServiceAccountPassword,
            "(/v1/userprofile|/refdata/).*"
        );
    }

}
