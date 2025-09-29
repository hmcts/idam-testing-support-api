package uk.gov.hmcts.cft.idam.api.v2.common.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.List;

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
        private IdamFrontend idamFrontend;

        @Pattern(regexp = "http(s)?://.*")
        private String postActivationRedirectUrl;

        private List<String> ssoProviders;
        private List<String> onboardingRoleNames;
    }

    @Getter
    @Setter
    public class OAuth2 {
        private boolean issuerOverride;
        private RequiredIssuer requiredIssuer;

        private List<String> grantTypes;
        private List<String> scopes;

        private List<@Pattern(regexp = "http(s)?://.*") String> redirectUris;

        private Duration accessTokenLifetime;
        private Duration refreshTokenLifetime;
    }

}
