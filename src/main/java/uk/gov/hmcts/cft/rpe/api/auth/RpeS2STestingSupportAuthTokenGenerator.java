package uk.gov.hmcts.cft.rpe.api.auth;

import uk.gov.hmcts.cft.rpe.api.RpeS2STestingSupportApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

public class RpeS2STestingSupportAuthTokenGenerator implements AuthTokenGenerator {

    private final String serviceName;
    private final RpeS2STestingSupportApi rpeS2STestingSupportApi;

    public RpeS2STestingSupportAuthTokenGenerator(String serviceName, RpeS2STestingSupportApi rpeS2STestingSupportApi) {
        this.serviceName = serviceName;
        this.rpeS2STestingSupportApi = rpeS2STestingSupportApi;
    }

    @Override
    public String generate() {
        return rpeS2STestingSupportApi.lease(serviceName);
    }

}
