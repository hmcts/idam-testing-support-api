package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.cft.idam.api.v2.IdamV2UserManagementApi;
import uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestingUserServiceTest {

    @Mock
    IdamV2UserManagementApi idamV2UserManagementApi;

    @Mock
    TestingEntityRepo testingEntityRepo;

    @InjectMocks
    TestingUserService underTest;

    @Captor
    ArgumentCaptor<TestingEntity> testingEntityArgumentCaptor;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(underTest, "expiredBurnerUserBatchSize", 10);
    }


    /**
     * @verifies create user and testing entity
     * @see TestingUserService#createTestUser(String, uk.gov.hmcts.cft.idam.api.v2.common.model.User, String)
     */
    @Test
    public void createTestUser_shouldCreateUserAndTestingEntity() throws Exception {
        User testUser = new User();
        testUser.setId("test-user-id");
        when(idamV2UserManagementApi.createUser(any())).thenReturn(testUser);
        when(testingEntityRepo.save(any())).then(returnsFirstArg());
        String sessionId = UUID.randomUUID().toString();
        User result = underTest.createTestUser(sessionId, testUser, "test-secret");
        assertEquals(testUser, result);

        verify(testingEntityRepo, times(1)).save(testingEntityArgumentCaptor.capture());

        TestingEntity testingEntity = testingEntityArgumentCaptor.getValue();

        assertEquals("test-user-id", testingEntity.getEntityId());
        assertEquals(sessionId, testingEntity.getTestingSessionId());
        assertEquals(TestingEntityType.USER, testingEntity.getEntityType());
        assertNotNull(testingEntity.getCreateDate());
    }

    /**
     * @verifies create user and testing entity with roles
     * @see TestingUserService#createTestUser(String, User, String)
     */
    @Test
    public void createTestUser_shouldCreateUserAndTestingEntityWithRoles() throws Exception {
        User testUser = new User();
        testUser.setId("test-user-id");
        testUser.setRoleNames(Collections.singletonList("test-role-1"));
        when(idamV2UserManagementApi.createUser(any())).thenReturn(testUser);
        when(testingEntityRepo.save(any())).then(returnsFirstArg());
        String sessionId = UUID.randomUUID().toString();
        User result = underTest.createTestUser(sessionId, testUser, "test-secret");
        assertEquals(testUser, result);
        verify(testingEntityRepo, times(1)).save(testingEntityArgumentCaptor.capture());

        TestingEntity testingEntity = testingEntityArgumentCaptor.getValue();

        assertEquals("test-user-id", testingEntity.getEntityId());
        assertEquals(sessionId, testingEntity.getTestingSessionId());
        assertEquals(TestingEntityType.USER, testingEntity.getEntityType());
        assertNotNull(testingEntity.getCreateDate());
    }

    /**
     * @verifies get expired burner users
     * @see TestingUserService#getExpiredBurnerUserTestingEntities(java.time.ZonedDateTime)
     */
    @Test
    public void getExpiredBurnerUserTestingEntities_shouldGetExpiredBurnerUsers() throws Exception {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        TestingEntity testingEntity = new TestingEntity();
        Page<TestingEntity> testPage = new PageImpl<>(Collections.singletonList(testingEntity));
        when(
            testingEntityRepo
                .findByEntityTypeAndCreateDateBeforeAndTestingSessionIdIsNullOrderByCreateDateAsc(any(), any(), any()))
            .thenReturn(testPage);
        List<TestingEntity> result = underTest.getExpiredBurnerUserTestingEntities(zonedDateTime);
        assertEquals(testingEntity, result.get(0));
    }

    @Test
    public void deleteIdamUserIfPresent_shouldDeleteUserAndTestingEntityIfPresent() throws Exception {
        User testUser = new User();
        testUser.setId("test-user-id");

        TestingEntity testingEntity = new TestingEntity();
        testingEntity.setEntityId("test-user-id");

        assertTrue(underTest.delete("test-user-id"));

        verify(idamV2UserManagementApi, times(1)).deleteUser(eq("test-user-id"));
    }

    @Test
    public void deleteIdamUserIfPresent_shouldReturnEmptyIfNoUser() throws Exception {
        doThrow(SpringWebClientHelper.notFound()).when(idamV2UserManagementApi).deleteUser("test-user-id");
        assertFalse(underTest.delete("test-user-id"));
    }

    /**
     * @verifies report if created roles do not match request
     * @see TestingUserService#createTestUser(String, User, String)
     */
    @Test
    public void createTestUser_shouldReportIfCreatedRolesDoNotMatchRequest() throws Exception {
        User requestUser = new User();
        requestUser.setId("test-user-id");
        requestUser.setRoleNames(Collections.singletonList("invalid-role-1"));
        User testUser = new User();
        testUser.setId("test-user-id");
        when(idamV2UserManagementApi.createUser(any())).thenReturn(testUser);
        when(testingEntityRepo.save(any())).then(returnsFirstArg());
        String sessionId = UUID.randomUUID().toString();
        User result = underTest.createTestUser(sessionId, requestUser, "test-secret");
        assertEquals(testUser, result);

        requestUser.setRoleNames(Collections.emptyList());
        testUser.setRoleNames(Collections.singletonList("extra-role-1"));
        when(idamV2UserManagementApi.createUser(any())).thenReturn(testUser);
        when(testingEntityRepo.save(any())).then(returnsFirstArg());
        result = underTest.createTestUser(sessionId, requestUser, "test-secret");
        assertEquals(testUser, result);

        requestUser.setRoleNames(Collections.singletonList("test-role-1"));
        testUser.setRoleNames(Collections.singletonList("test-role-2"));
        when(idamV2UserManagementApi.createUser(any())).thenReturn(testUser);
        when(testingEntityRepo.save(any())).then(returnsFirstArg());
        result = underTest.createTestUser(sessionId, requestUser, "test-secret");
        assertEquals(testUser, result);

        requestUser.setRoleNames(Collections.singletonList("test-role-1"));
        testUser.setRoleNames(Arrays.asList("test-role-1", "test-role-2"));
        when(idamV2UserManagementApi.createUser(any())).thenReturn(testUser);
        when(testingEntityRepo.save(any())).then(returnsFirstArg());
        result = underTest.createTestUser(sessionId, requestUser, "test-secret");
        assertEquals(testUser, result);
    }

}
