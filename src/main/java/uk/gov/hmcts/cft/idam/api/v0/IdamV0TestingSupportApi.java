package uk.gov.hmcts.cft.idam.api.v0;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.idam.api.internal.model.Account;
import uk.gov.hmcts.reform.idam.api.internal.model.TestUserRequest;

@FeignClient(name = "idamv0testingsupport", url = "${idam.api.url}")
public interface IdamV0TestingSupportApi {

    @PostMapping("/testing-support/accounts")
    Account createTestUser(@RequestBody TestUserRequest request);

}
