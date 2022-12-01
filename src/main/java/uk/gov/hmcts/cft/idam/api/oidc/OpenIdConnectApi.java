package uk.gov.hmcts.cft.idam.api.oidc;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "openidconnectapi", url = "${idam.api.url}")
public interface OpenIdConnectApi {

    String PASSWORD_GRANT = "password";
    String ACCESS_TOKEN = "access_token";

    default String passwordGrant(String username,
                         String userSecret,
                         String clientId,
                         String clientSecret,
                         String scope) {
        return createToken(PASSWORD_GRANT, username, userSecret, clientId, clientSecret, scope).get(ACCESS_TOKEN);
    }

    @PostMapping(value = "/o/token", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    Map<String, String> createToken(@RequestParam("grant_type") String grantType,
                                    @RequestParam("username") String username,
                                    @RequestParam("password") String userSecret,
                                    @RequestParam("client_id") String clientId,
                                    @RequestParam("client_secret") String clientSecret,
                                    @RequestParam("scope") String scope);

}
