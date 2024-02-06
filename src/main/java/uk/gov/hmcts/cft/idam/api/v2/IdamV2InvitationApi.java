package uk.gov.hmcts.cft.idam.api.v2;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.hmcts.cft.idam.api.v2.common.auth.IdamClientCredentialsConfig;
import uk.gov.hmcts.cft.idam.api.v2.common.model.Invitation;

import java.util.List;

@FeignClient(name = "idamv2invitation", url = "${idam.api.url}", configuration = IdamClientCredentialsConfig.class)
public interface IdamV2InvitationApi {

    @GetMapping("/api/v2/invitations-by-user-email/{email}")
    List<Invitation> getInvitationsByUserEmail(@PathVariable String email);

}
