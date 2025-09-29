package uk.gov.hmcts.cft.idam.api.v2.common.model;

import java.util.Optional;

public enum RequiredIssuer {
    IDAM, FORGEROCK;

    public static Optional<RequiredIssuer> findValue(String value) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(value));
        } catch (IllegalArgumentException iae) {
            return Optional.empty();
        }
    }
}
