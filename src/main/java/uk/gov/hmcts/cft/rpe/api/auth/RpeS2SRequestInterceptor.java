package uk.gov.hmcts.cft.rpe.api.auth;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import uk.gov.hmcts.cft.rpe.api.RpeS2STestingSupportApi;

import java.util.regex.Pattern;

public class RpeS2SRequestInterceptor implements RequestInterceptor {

    private static final String SERVICE_AUTH_HEADER = "ServiceAuthorization";

    private static final String BEARER = "Bearer";

    private final RpeS2STestingSupportApi rpeS2STestingSupportApi;

    private final String serviceName;

    private final Pattern matchesPattern;

    public RpeS2SRequestInterceptor(RpeS2STestingSupportApi rpeS2STestingSupportApi, String serviceName,
                                    String matchesRegex) {
        this.rpeS2STestingSupportApi = rpeS2STestingSupportApi;
        this.serviceName = serviceName;
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
        return rpeS2STestingSupportApi.lease(serviceName);
    }

}
