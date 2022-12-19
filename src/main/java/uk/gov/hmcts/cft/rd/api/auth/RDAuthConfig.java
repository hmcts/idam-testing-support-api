package uk.gov.hmcts.cft.rd.api.auth;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.cft.idam.api.oidc.OpenIdConnectApi;
import uk.gov.hmcts.cft.idam.api.oidc.auth.PasswordGrantRequestInterceptor;
import uk.gov.hmcts.cft.rpe.api.RpeS2STestingSupportApi;
import uk.gov.hmcts.cft.rpe.api.auth.RpeS2SRequestInterceptor;

public class RDAuthConfig {

    @Value("${rd.userprofile.api.s2s.servicename}")
    String rdServiceName;

    @Value("${rd.userprofile.api.registration.useremail}")
    String passwordGrantUserEmail;

    @Value("${rd.userprofile.api.registration.usersecret}")
    String passwordGrantUserSecret;

    @Value("${rd.userprofile.api.registration.client-id}")
    String clientId;

    @Value("${rd.userprofile.api.registration.client-secret}")
    String clientSecret;

    @Value("${rd.userprofile.api.registration.scopes}")
    String passwordGrantScopes;

    @Bean
    public RequestInterceptor rdServiceAuthorizationInterceptor(
        RpeS2STestingSupportApi rpeS2STestingSupportApi) {
        return new RpeS2SRequestInterceptor(rpeS2STestingSupportApi, rdServiceName, "/v1/userprofile/.*");
    }

    @Bean RequestInterceptor rdPasswordGrantInterceptor(
        OpenIdConnectApi openIdConnectApi) {
        return new PasswordGrantRequestInterceptor(openIdConnectApi, "/v1/userprofile/.*",
                                                   passwordGrantUserEmail,
                                                   passwordGrantUserSecret,
                                                   clientId,
                                                   clientSecret,
                                                   passwordGrantScopes
        );
    }

}
