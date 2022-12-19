package uk.gov.hmcts.cft.idam.api.oidc.auth;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import uk.gov.hmcts.cft.idam.api.oidc.OpenIdConnectApi;

import java.util.regex.Pattern;

public class PasswordGrantRequestInterceptor implements RequestInterceptor {

    private static final String AUTH_HEADER = "Authorization";

    private static final String BEARER = "Bearer";

    private final OpenIdConnectApi openIdConnectApi;

    private final Pattern matchesPattern;

    private final String userName;
    private final String userSecret;
    private final String clientId;
    private final String clientSecret;
    private final String scopes;

    public PasswordGrantRequestInterceptor(OpenIdConnectApi openIdConnectApi, String matchesRegex, String userId,
                                           String userSecret, String clientId, String clientSecret, String scopes) {
        this.openIdConnectApi = openIdConnectApi;
        this.matchesPattern = Pattern.compile(matchesRegex);
        this.userName = userId;
        this.userSecret = userSecret;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scopes = scopes;
    }

    @Override
    public void apply(RequestTemplate template) {
        if (handleUrl(template.url())) {
            addBearer(template, getAccessToken());
        }
    }

    private boolean handleUrl(String url) {
        return url != null && matchesPattern.matcher(url).find();
    }

    private void addBearer(RequestTemplate template, String token) {
        template.header(AUTH_HEADER, BEARER + " " + token);
    }

    private String getAccessToken() {
        return openIdConnectApi.passwordGrant(userName, userSecret, clientId, clientSecret, scopes);
    }

}
