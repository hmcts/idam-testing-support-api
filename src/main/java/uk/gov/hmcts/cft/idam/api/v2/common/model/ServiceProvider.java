package uk.gov.hmcts.cft.idam.api.v2.common.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class ServiceProvider {

    @NotNull
    private String clientId;

    private String clientSecret;

    private String description;

    @Valid
    private HmctsAccess hmctsAccess;

    @Valid
    private OAuth2 oAuth2;

    @Getter
    @Setter
    public class HmctsAccess {
        private boolean mfaRequired;
        private boolean selfRegistrationAllowed;

        @Pattern(regexp = "http(s)?://.*")
        private String postActivationRedirectUrl;

        private List<String> ssoProviders;
        private List<String> onboardingRoleNames;
    }

    @Getter
    @Setter
    public class OAuth2 {
        private boolean issuerOverride;

        private List<String> grantTypes;
        private List<String> scopes;

        private List<@Pattern(regexp = "http(s)?://.*") String> redirectUris;

        private Duration accessTokenLifetime;
        private Duration refreshTokenLifetime;
    }

}
