package uk.gov.hmcts.cft.rd.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.cft.rd.api.auth.RefDataAuthConfig;
import uk.gov.hmcts.cft.rd.model.CaseWorkerProfile;

@FeignClient(name = "rdcaseworkerapi", url = "${rd.caseworker.api.url}", configuration = RefDataAuthConfig.class)
public interface RefDataCaseWorkerApi {

    @PutMapping("/refdata/case-worker/users/sync")
    void updateCaseWorkerProfile(@RequestBody CaseWorkerProfile caseWorkerProfile);

    @GetMapping("/refdata/case-worker/profile/search-by-id")
    CaseWorkerProfile findCaseWorkerProfileByUserId(@RequestParam String id);

}
