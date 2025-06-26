package uk.gov.hmcts.cft.idam.api.v2;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.hmcts.cft.idam.api.v2.common.model.Invitation;

import java.util.List;

@FeignClient(name = "idamv2invitation", url = "${idam.api.url}")
public interface IdamV2InvitationApi {

    @GetMapping("/api/v2/invitations-by-user-email/{email}")
    List<Invitation> getInvitationsByUserEmail(@PathVariable String email);

    @PostMapping("/api/v2/invitations")
    Invitation createInvitation(Invitation invitation);

    @DeleteMapping("/api/v2/invitations/{id}")
    Invitation revokeInvitation(@PathVariable String id);

}
