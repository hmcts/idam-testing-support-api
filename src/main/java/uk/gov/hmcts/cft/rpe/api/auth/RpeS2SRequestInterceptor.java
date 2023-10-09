package uk.gov.hmcts.cft.rpe.api.auth;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.regex.Pattern;

public class RpeS2SRequestInterceptor implements RequestInterceptor {

    private static final String SERVICE_AUTH_HEADER = "ServiceAuthorization";

    private static final String BEARER = "Bearer";

    private final AuthTokenGenerator authTokenGenerator;

    private final Pattern matchesPattern;

    public RpeS2SRequestInterceptor(AuthTokenGenerator authTokenGenerator,
                                    String matchesRegex) {
        this.authTokenGenerator = authTokenGenerator;
        this.matchesPattern = Pattern.compile(matchesRegex);
    }

    @Override
    public void apply(RequestTemplate template) {
        if (handleUrl(template.url())) {
            addServiceBearer(template, getS2SToken());
        }
    }

    private boolean handleUrl(String url) {
        return url != null && matchesPattern.matcher(url).find();
    }

    private void addServiceBearer(RequestTemplate template, String token) {
        template.header(SERVICE_AUTH_HEADER, BEARER + " " + token);
    }

    private String getS2SToken() {
        return authTokenGenerator.generate();
    }

}
