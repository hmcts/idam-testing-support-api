package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cft.idam.api.v1.usermanagement.IdamV1UserManagementApi;
import uk.gov.hmcts.cft.idam.api.v1.usermanagement.model.User;
import uk.gov.hmcts.cft.idam.api.v2.IdamV2ConfigApi;
import uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper;
import uk.gov.hmcts.cft.idam.api.v2.common.model.Role;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestingRoleServiceTest {

    @Mock
    IdamV2ConfigApi idamV2ConfigApi;

    @Mock
    TestingEntityRepo testingEntityRepo;

    @Mock
    IdamV1UserManagementApi idamV1UserManagementApi;

    @Mock
    TestingUserService testingUserService;

    @InjectMocks
    TestingRoleService underTest;

    @Captor
    ArgumentCaptor<TestingEntity> testingEntityArgumentCaptor;

    /**
     * @verifies create role and testing entity
     * @see TestingRoleService#createTestRole(String, uk.gov.hmcts.cft.idam.api.v2.common.model.Role)
     */
    @Test
    void createTestRole_shouldCreateRoleAndTestingEntity() throws Exception {
        Role testRole = new Role();
        testRole.setName("test-role-name");
        when(idamV2ConfigApi.createRole(any())).then(returnsFirstArg());
        when(testingEntityRepo.save(any())).then(returnsFirstArg());
        String sessionId = UUID.randomUUID().toString();
        Role result = underTest.createTestRole(sessionId, testRole);
        assertEquals(testRole, result);

        verify(testingEntityRepo, times(1)).save(testingEntityArgumentCaptor.capture());
        TestingEntity testingEntity = testingEntityArgumentCaptor.getValue();

        assertEquals("test-role-name", testingEntity.getEntityId());
        assertEquals(sessionId, testingEntity.getTestingSessionId());
        assertEquals(TestingEntityType.ROLE, testingEntity.getEntityType());
        assertNotNull(testingEntity.getCreateDate());
    }

    /**
     * @verifies delete role
     * @see TestingRoleService#deleteEntity(String)
     */
    @Test
    void deleteEntity_shouldDeleteRole() throws Exception {
        assertTrue(underTest.delete("test-entity-id"));
        verify(idamV2ConfigApi, times(1)).deleteRole("test-entity-id");
    }

    /**
     * @verifies get entity key
     * @see TestingRoleService#getEntityKey(uk.gov.hmcts.cft.idam.api.v2.common.model.Role)
     */
    @Test
    void getEntityKey_shouldGetEntityKey() throws Exception {
        Role testRole = new Role();
        testRole.setName("test-role-name");
        assertEquals("test-role-name", underTest.getEntityKey(testRole));
    }

    /**
     * @verifies get entity type
     * @see TestingRoleService#getTestingEntityType()
     */
    @Test
    void getTestingEntityType_shouldGetEntityType() throws Exception {
        assertEquals(underTest.getTestingEntityType(), TestingEntityType.ROLE);
    }

    @Test
    void handleCleanupException_preconditionFailureForSingleUser() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        cleanupEntity.setEntityId("entity-id");
        User foundUser = new User();
        foundUser.setId("found-user-id");
        when(idamV1UserManagementApi.searchUsers(any(), any(), any())).thenReturn(List.of(foundUser));
        assertTrue(underTest.handleCleanupException(SpringWebClientHelper.precondition(), TestingEntityService.CleanupFailureStrategy.CUSTOM, cleanupEntity));
        verify(testingUserService, times(1)).forceRemoveTestUser("found-user-id");
    }

    @Test
    void handleCleanupException_preconditionFailureNoUsers() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        cleanupEntity.setEntityId("entity-id");
        User foundUser = new User();
        foundUser.setId("found-user-id");
        when(idamV1UserManagementApi.searchUsers(any(), any(), any())).thenReturn(Collections.emptyList());
        assertFalse(underTest.handleCleanupException(SpringWebClientHelper.precondition(), TestingEntityService.CleanupFailureStrategy.CUSTOM, cleanupEntity));
        verify(testingUserService, never()).forceRemoveTestUser("found-user-id");
    }

    @Test
    void handleCleanupException_preconditionFailureForTwoUsers() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        cleanupEntity.setEntityId("entity-id");
        User foundUser1 = new User();
        foundUser1.setId("found-user-id-1");
        User foundUser2 = new User();
        foundUser2.setId("found-user-id-2");
        when(idamV1UserManagementApi.searchUsers(any(), any(), any())).thenReturn(List.of(foundUser1, foundUser2));
        assertFalse(underTest.handleCleanupException(SpringWebClientHelper.precondition(), TestingEntityService.CleanupFailureStrategy.CUSTOM, cleanupEntity));
        verify(testingUserService, never()).forceRemoveTestUser(any());
    }

    @Test
    void handleCleanupException_otherException() {
        CleanupEntity cleanupEntity = new CleanupEntity();
        cleanupEntity.setEntityId("entity-id");
        User foundUser = new User();
        foundUser.setId("found-user-id");
        assertFalse(underTest.handleCleanupException(new RuntimeException(), TestingEntityService.CleanupFailureStrategy.CUSTOM, cleanupEntity));
        verify(testingUserService, never()).forceRemoveTestUser("found-user-id");
    }

}
