package uk.gov.hmcts.cft.idam.api.oidc;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "openidconnectapi", url = "${idam.api.url}")
public interface OpenIdConnectApi {
    
    void createToken(String grantType, String username, String userSecret, String clientId, String clientSecret, String scopes);

}
