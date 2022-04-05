package uk.gov.hmcts.cft.idam.testingsupportapi.util;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collections;
import java.util.Optional;

public class PrincipalHelper {

    private PrincipalHelper() {
    }

    /**
     * Get session key.
     * @should return key value if present
     * @should return default value if key is not present
     */
    public static String getSessionKey(Jwt principal) {
        if (principal.hasClaim("auditTrackingId")) {
            return principal.getClaimAsString("auditTrackingId");
        }
        return "" + principal.hashCode();
    }

    /**
     * Get client id.
     * @should return first client id if present
     * @should return optional empty if no client id is present
     */
    public static Optional<String> getClientId(Jwt principal) {
        return Optional.ofNullable(principal.getClaimAsStringList("aud"))
            .orElse(Collections.emptyList())
            .stream().findFirst();
    }

}
