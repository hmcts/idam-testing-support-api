package uk.gov.hmcts.cft.idam.api.v2.common.model;

import java.util.Optional;

public enum IdamFrontend {
    HMCTS_ACCESS, HMCTS_CLASSIC;

    public static Optional<IdamFrontend> findValue(String value) {
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
