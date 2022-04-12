package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.cft.idam.api.v0.IdamV0TestingSupportApi;
import uk.gov.hmcts.cft.idam.api.v2.common.model.AccountStatus;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.reform.idam.api.internal.model.Account;
import uk.gov.hmcts.reform.idam.api.internal.model.TestUserRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IdamV0ServiceTest {

    @Mock
    IdamV0TestingSupportApi idamV0TestingSupportApi;

    @InjectMocks
    IdamV0Service underTest;

    /**
     * @verifies create test user
     * @see IdamV0Service#createTestUser(uk.gov.hmcts.cft.idam.api.v2.common.model.User, String)
     */
    @Test
    public void createTestUser_shouldCreateTestUser() throws Exception {
        Account account = new Account();
        account.setId("1234");
        account.setActive(true);

        User testUser = new User();

        when(idamV0TestingSupportApi.createTestUser(any())).thenReturn(account);

        User result = underTest.createTestUser(testUser, "test-secret");

        assertEquals(testUser, result);
        assertEquals(account.getId(), result.getId());
        assertEquals(AccountStatus.ACTIVE, result.getAccountStatus());
    }

    /**
     * @verifies add account details to user
     * @see IdamV0Service#mergeWithAccount(uk.gov.hmcts.cft.idam.api.v2.common.model.User, uk.gov.hmcts.reform.idam.api.internal.model.Account)
     */
    @Test
    public void mergeWithAccount_shouldAddAccountDetailsToUser() throws Exception {
        Account account = new Account();
        account.setId("1234");
        account.setActive(true);
        account.setLastModified("2022-03-31T16:00:00Z");

        User testUser = new User();
        testUser.setId("1234");

        testUser = underTest.mergeWithAccount(testUser, account);

        assertEquals(account.getId(), testUser.getId());
        assertEquals(AccountStatus.ACTIVE, testUser.getAccountStatus());
        assertNotNull(testUser.getCreateDate());
        assertNotNull(testUser.getLastModified());
    }

    /**
     * @verifies ignore mismatched roles
     * @see IdamV0Service#mergeWithAccount(uk.gov.hmcts.cft.idam.api.v2.common.model.User, uk.gov.hmcts.reform.idam.api.internal.model.Account)
     */
    @Test
    public void mergeWithAccount_shouldIgnoreMismatchedRoles() throws Exception {
        Account account = new Account();
        account.setId("1234");
        account.setActive(true);
        account.setLastModified("2022-03-31T16:00:00Z");
        account.setRoles(Arrays.asList("citizen", "test-role-2"));

        User testUser = new User();
        testUser.setId("1234");
        testUser.setRoleNames(Collections.singletonList("citizen"));

        testUser = underTest.mergeWithAccount(testUser, account);

        assertEquals(account.getId(), testUser.getId());
        assertEquals(AccountStatus.ACTIVE, testUser.getAccountStatus());
        assertNotNull(testUser.getCreateDate());
        assertNotNull(testUser.getLastModified());
    }

    /**
     * @verifies set deactivated accounts
     * @see IdamV0Service#mergeWithAccount(uk.gov.hmcts.cft.idam.api.v2.common.model.User, uk.gov.hmcts.reform.idam.api.internal.model.Account)
     */
    @Test
    public void mergeWithAccount_shouldSetDeactivatedAccounts() throws Exception {
        Account account = new Account();
        account.setId("1234");
        account.setActive(false);
        account.setLastModified("2022-03-31T16:00:00Z");

        User testUser = new User();
        testUser.setId("1234");

        testUser = underTest.mergeWithAccount(testUser, account);

        assertEquals(account.getId(), testUser.getId());
        assertEquals(AccountStatus.DEACTIVATED, testUser.getAccountStatus());
        assertNotNull(testUser.getCreateDate());
        assertNotNull(testUser.getLastModified());
    }

    /**
     * @verifies build test user request
     * @see IdamV0Service#buildTestUserRequest(uk.gov.hmcts.cft.idam.api.v2.common.model.User, String)
     */
    @Test
    public void buildTestUserRequest_shouldBuildTestUserRequest() throws Exception {
        User testUser = new User();
        testUser.setId("1234");
        testUser.setEmail("test@test.local");
        testUser.setForename("test-forename");
        testUser.setSurname("test-surname");
        testUser.setSsoId("test-sso-id");
        testUser.setSsoProvider("test-provider");
        testUser.setRoleNames(Collections.singletonList("citizen"));

        TestUserRequest request = underTest.buildTestUserRequest(testUser, "test-secret");
        assertEquals(testUser.getId(), request.getId());
        assertEquals(testUser.getEmail(), request.getEmail());
        assertEquals(testUser.getForename(), request.getForename());
        assertEquals(testUser.getSurname(), request.getSurname());
        assertEquals(testUser.getSsoId(), request.getSsoId());
        assertEquals(testUser.getSsoProvider(), request.getSsoProvider());
        assertEquals("test-secret", request.getPassword());
        assertEquals("citizen", request.getRoles().get(0).getCode());
    }

    /**
     * @verifies return user if exists
     * @see IdamV0Service#findUserById(String)
     */
    @Test
    public void findUserById_shouldReturnUserIfExists() throws Exception {
        Account account = new Account();
        account.setId("1234");
        account.setActive(false);
        account.setLastModified("2022-03-31T16:00:00Z");

        when(idamV0TestingSupportApi.getAccount(eq("1234"))).thenReturn(account);

        Optional<User> result = underTest.findUserById("1234");
        assertEquals(result.get().getId(), account.getId());
    }

    /**
     * @verifies return empty if no user
     * @see IdamV0Service#findUserById(String)
     */
    @Test
    public void findUserById_shouldReturnEmptyIfNoUser() throws Exception {
        when(idamV0TestingSupportApi.getAccount(eq("1234")))
            .thenThrow(
                new HttpClientErrorException(HttpStatus.NOT_FOUND));

        Optional<User> result = underTest.findUserById("1234");
        assertEquals(Optional.empty(), result);
    }

    /**
     * @verifies delete user
     * @see IdamV0Service#deleteUser(User)
     */
    @Test
    public void deleteUser_shouldDeleteUser() throws Exception {
        User testUser = new User();
        testUser.setId("1234");
        testUser.setEmail("test@test.local");
        underTest.deleteUser(testUser);
        verify(idamV0TestingSupportApi, times(1)).deleteAccount(eq(testUser.getEmail()));
    }
}
