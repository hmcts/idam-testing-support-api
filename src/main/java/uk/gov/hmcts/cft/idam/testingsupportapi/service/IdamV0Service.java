package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cft.idam.api.v0.IdamV0TestingSupportApi;
import uk.gov.hmcts.cft.idam.api.v2.common.model.AccountStatus;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.reform.idam.api.internal.model.Account;
import uk.gov.hmcts.reform.idam.api.internal.model.TestUserRequest;
import uk.gov.hmcts.reform.idam.api.shared.model.RoleDetail;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class IdamV0Service {

    private final IdamV0TestingSupportApi idamV0TestingSupportApi;

    public IdamV0Service(IdamV0TestingSupportApi idamV0TestingSupportApi) {
        this.idamV0TestingSupportApi = idamV0TestingSupportApi;
    }

    /**
     * Creates a test user.
     * @should create test user
     */
    public User createTestUser(User requestUser, String secretPhrase) {
        TestUserRequest testUserRequest = buildTestUserRequest(requestUser, secretPhrase);
        Account result = idamV0TestingSupportApi.createTestUser(testUserRequest);
        return mergeWithAccount(requestUser, result);
    }

    /**
     * Merge Account details with user.
     * @should add account details to user
     * @should ignore mismatched roles
     * @should set deactivated accounts
     */
    protected User mergeWithAccount(User requestUser, Account account) {
        if (!account.getId().equals(requestUser.getId())) {
            requestUser.setId(account.getId());
        }
        // Accounts don't include stale info
        if (account.isActive()) {
            requestUser.setAccountStatus(AccountStatus.ACTIVE);
        } else {
            requestUser.setAccountStatus(AccountStatus.DEACTIVATED);
        }
        // Not sure why accounts don't include create date
        requestUser.setCreateDate(parseDateTime(account.getLastModified()));
        requestUser.setLastModified(parseDateTime(account.getLastModified()));

        if (CollectionUtils.size(account.getRoles()) != CollectionUtils.size(requestUser.getRoleNames())) {
            log.info(
                "User {} created with different number of roles than requested. Requested names: {}, Actual ids: {}",
                requestUser.getId(),
                requestUser.getRoleNames(),
                account.getRoles());
        }
        return requestUser;
    }

    /**
     * build test user request.
     * @should build test user request
     */
    protected TestUserRequest buildTestUserRequest(User requestUser, String secretPhrase) {
        TestUserRequest testUserRequest = new TestUserRequest();
        testUserRequest.setEmail(requestUser.getEmail());
        testUserRequest.setId(requestUser.getId());
        testUserRequest.setForename(requestUser.getForename());
        testUserRequest.setSurname(requestUser.getSurname());
        testUserRequest.setPassword(secretPhrase);
        testUserRequest.setSsoId(requestUser.getSsoId());
        testUserRequest.setSsoProvider(requestUser.getSsoProvider());
        testUserRequest.setUserGroup(null); // deprecated
        if (CollectionUtils.isNotEmpty(requestUser.getRoleNames())) {
            List<RoleDetail> roleDetailList = new ArrayList<>();
            for (String roleName : requestUser.getRoleNames()) {
                RoleDetail roleDetail = new RoleDetail();
                roleDetail.setCode(roleName);
                roleDetailList.add(roleDetail);
            }
            testUserRequest.setRoles(roleDetailList);
        }
        return testUserRequest;
    }

    private ZonedDateTime parseDateTime(String value) {
        if (value != null) {
            return ZonedDateTime.parse(value);
        }
        return null;
    }

}
