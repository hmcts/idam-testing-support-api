package uk.gov.hmcts.cft.idam.api.v2;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.cft.idam.api.v2.common.model.Role;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ServiceProvider;

@FeignClient(name = "idamv2config", url = "${idam.api.url}")
public interface IdamV2ConfigApi {

    @PostMapping("/api/v2/roles")
    Role createRole(@Valid @RequestBody Role role);

    @DeleteMapping("/api/v2/roles/{roleName}")
    void deleteRole(@PathVariable String roleName);

    @PostMapping("/api/v2/services")
    ServiceProvider createService(@Valid @RequestBody ServiceProvider service);

    @DeleteMapping("/api/v2/services/{clientId}")
    void deleteService(@PathVariable String clientId);


}
