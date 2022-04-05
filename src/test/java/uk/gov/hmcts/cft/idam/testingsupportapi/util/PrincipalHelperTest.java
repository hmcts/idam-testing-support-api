package uk.gov.hmcts.cft.idam.testingsupportapi.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PrincipalHelperTest {

    @Mock
    Jwt principal;

    /**
     * @verifies return key value if present
     * @see PrincipalHelper#getSessionKey(org.springframework.security.oauth2.jwt.Jwt)
     */
    @Test
    public void getSessionKey_shouldReturnKeyValueIfPresent() throws Exception {
        when(principal.hasClaim("auditTrackingId")).thenReturn(true);
        when(principal.getClaimAsString("auditTrackingId")).thenReturn("test-key");
        assertEquals("test-key", PrincipalHelper.getSessionKey(principal));
    }

    /**
     * @verifies return default value if key is not present
     * @see PrincipalHelper#getSessionKey(org.springframework.security.oauth2.jwt.Jwt)
     */
    @Test
    public void getSessionKey_shouldReturnDefaultValueIfKeyIsNotPresent() throws Exception {
        when(principal.hasClaim("auditTrackingId")).thenReturn(false);
        assertEquals("" + principal.hashCode(), PrincipalHelper.getSessionKey(principal));
    }

    /**
     * @verifies return first client id if present
     * @see PrincipalHelper#getClientId(org.springframework.security.oauth2.jwt.Jwt)
     */
    @Test
    public void getClientId_shouldReturnFirstClientIdIfPresent() throws Exception {
        when(principal.getClaimAsStringList("aud")).thenReturn(Collections.singletonList("test-client"));
        assertEquals("test-client", PrincipalHelper.getClientId(principal).get());
        when(principal.getClaimAsStringList("aud")).thenReturn(Arrays.asList("test-client1", "test-client2"));
        assertEquals("test-client1", PrincipalHelper.getClientId(principal).get());
    }

    /**
     * @verifies return optional empty if no client id is present
     * @see PrincipalHelper#getClientId(org.springframework.security.oauth2.jwt.Jwt)
     */
    @Test
    public void getClientId_shouldReturnOptionalEmptyIfNoClientIdIsPresent() throws Exception {
        when(principal.getClaimAsStringList("aud")).thenReturn(null);
        assertTrue(PrincipalHelper.getClientId(principal).isEmpty());
        when(principal.getClaimAsStringList("aud")).thenReturn(Collections.emptyList());
        assertTrue(PrincipalHelper.getClientId(principal).isEmpty());
    }
}
