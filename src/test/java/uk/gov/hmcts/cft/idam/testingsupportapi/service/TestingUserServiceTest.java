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
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.cft.idam.api.v2.IdamV2UserManagementApi;
import uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper;
import uk.gov.hmcts.cft.idam.api.v2.common.model.RecordType;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingState;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cft.idam.testingsupportapi.receiver.CleanupReceiver.CLEANUP_USER;

@ExtendWith(MockitoExtension.class)
class TestingUserServiceTest {

    private static final long EPOCH_1AM = 3600;

    @Mock
    IdamV2UserManagementApi idamV2UserManagementApi;

    @Mock
    TestingEntityRepo testingEntityRepo;

    @Mock
    JmsTemplate jmsTemplate;

    @InjectMocks
    TestingUserService underTest;

    @Captor
    ArgumentCaptor<TestingEntity> testingEntityArgumentCaptor;

    Clock testClock = Clock.fixed(Instant.ofEpochSecond(EPOCH_1AM), ZoneOffset.UTC);

    @BeforeEach
    void setup() {
        underTest.changeClock(testClock);
        ReflectionTestUtils.setField(underTest, "expiredBurnerUserBatchSize", 10);
        ReflectionTestUtils.setField(underTest, "recentLoginDuration", Duration.ofMinutes(10));
        ReflectionTestUtils.setField(underTest, "sessionLifespan", Duration.ofMinutes(20));
    }


    /**
     * @verifies create user and testing entity
     * @see TestingUserService#createTestUser(String, uk.gov.hmcts.cft.idam.api.v2.common.model.User, String)
     */
    @Test
    void createTestUser_shouldCreateUserAndTestingEntity() throws Exception {
        User testUser = new User();
        testUser.setId("test-user-id");
        when(idamV2UserManagementApi.createUser(any())).thenReturn(testUser);
        when(testingEntityRepo.save(any())).then(returnsFirstArg());
        String sessionId = UUID.randomUUID().toString();
        User result = underTest.createTestUser(sessionId, testUser, "test-secret");
        assertEquals(testUser, result);

        verify(testingEntityRepo, times(1)).save(testingEntityArgumentCaptor.capture());
        verify(idamV2UserManagementApi, never()).archiveUser("test-user-id");

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
    void createTestUser_shouldCreateUserAndTestingEntityWithRoles() throws Exception {
        User testUser = new User();
        testUser.setId("test-user-id");
        testUser.setRoleNames(Collections.singletonList("test-role-1"));
        when(idamV2UserManagementApi.createUser(any())).thenReturn(testUser);
        when(testingEntityRepo.save(any())).then(returnsFirstArg());
        String sessionId = UUID.randomUUID().toString();
        User result = underTest.createTestUser(sessionId, testUser, "test-secret");
        assertEquals(testUser, result);
        verify(testingEntityRepo, times(1)).save(testingEntityArgumentCaptor.capture());
        verify(idamV2UserManagementApi, never()).archiveUser("test-user-id");

        TestingEntity testingEntity = testingEntityArgumentCaptor.getValue();

        assertEquals("test-user-id", testingEntity.getEntityId());
        assertEquals(sessionId, testingEntity.getTestingSessionId());
        assertEquals(TestingEntityType.USER, testingEntity.getEntityType());
        assertNotNull(testingEntity.getCreateDate());
    }

    @Test
    void createTestUser_shouldCreateArchivedUserAndTestingEntity() throws Exception {
        User testUser = new User();
        testUser.setId("test-user-id");
        testUser.setRecordType(RecordType.ARCHIVED);
        when(idamV2UserManagementApi.createUser(any())).thenReturn(testUser);
        when(testingEntityRepo.save(any())).then(returnsFirstArg());
        String sessionId = UUID.randomUUID().toString();
        User result = underTest.createTestUser(sessionId, testUser, "test-secret");
        assertEquals(testUser, result);

        verify(testingEntityRepo, times(1)).save(testingEntityArgumentCaptor.capture());
        verify(idamV2UserManagementApi, times(1)).archiveUser("test-user-id");

        TestingEntity testingEntity = testingEntityArgumentCaptor.getValue();

        assertEquals("test-user-id", testingEntity.getEntityId());
        assertEquals(sessionId, testingEntity.getTestingSessionId());
        assertEquals(TestingEntityType.USER, testingEntity.getEntityType());
        assertNotNull(testingEntity.getCreateDate());
    }

    /**
     * @verifies update user and create testing entity
     * @see TestingUserService#updateTestUser(String, User, String)
     */
    @Test
    void updateTestUser_shouldUpdateUserAndCreateTestingEntity() throws Exception {
        User testUser = new User();
        testUser.setId("test-user-id");
        testUser.setRoleNames(Collections.singletonList("test-role-1"));
        when(idamV2UserManagementApi.updateUser(any(), any())).thenReturn(testUser);
        when(testingEntityRepo.save(any())).then(returnsFirstArg());
        String sessionId = UUID.randomUUID().toString();
        User result = underTest.updateTestUser(sessionId, testUser, "test-secret");
        assertEquals(testUser, result);
        verify(idamV2UserManagementApi).updateUserSecret("test-user-id", "test-secret");
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
    void getExpiredBurnerUserTestingEntities_shouldGetExpiredBurnerUsers() throws Exception {
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
    void deleteIdamUserIfPresent_shouldDeleteUserAndTestingEntityIfPresent() throws Exception {
        User testUser = new User();
        testUser.setId("test-user-id");

        TestingEntity testingEntity = new TestingEntity();
        testingEntity.setEntityId("test-user-id");

        assertTrue(underTest.delete("test-user-id"));

        verify(idamV2UserManagementApi, times(1)).deleteUser("test-user-id");
    }

    @Test
    void deleteIdamUserIfPresent_shouldReturnEmptyIfNoUser() throws Exception {
        doThrow(SpringWebClientHelper.notFound()).when(idamV2UserManagementApi).deleteUser("test-user-id");
        assertFalse(underTest.delete("test-user-id"));
    }

    /**
     * @verifies report if created roles do not match request
     * @see TestingUserService#createTestUser(String, User, String)
     */
    @Test
    void createTestUser_shouldReportIfCreatedRolesDoNotMatchRequest() throws Exception {
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

    /**
     * @verifies request cleanup of existing test entity
     * @see TestingUserService#addTestUserToSessionForRemoval(uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession, String)
     */
    @Test
    void addTestUserToSessionForRemoval_shouldRequestCleanupOfExistingTestEntity() throws Exception {
        TestingSession testingSession = new TestingSession();
        testingSession.setId(UUID.randomUUID().toString());

        TestingEntity testingEntity = new TestingEntity();
        testingEntity.setState(TestingState.ACTIVE);
        testingEntity.setEntityType(TestingEntityType.USER);

        when(testingEntityRepo.findAllByEntityIdAndEntityTypeAndState("test-user-id", TestingEntityType.USER,
                                                                      TestingState.ACTIVE))
            .thenReturn(Collections.singletonList(testingEntity));

        underTest.addTestUserToSessionForRemoval(testingSession, "test-user-id");

        verify(jmsTemplate).convertAndSend(eq(CLEANUP_USER), any(CleanupEntity.class));
        verify(testingEntityRepo, never()).save(any());
    }

    /**
     * @verifies add new test entity to session
     * @see TestingUserService#addTestUserToSessionForRemoval(uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession, String)
     */
    @Test
    void addTestUserToSessionForRemoval_shouldAddNewTestEntityToSession() throws Exception {
        TestingSession testingSession = new TestingSession();
        testingSession.setId(UUID.randomUUID().toString());

        when(testingEntityRepo.findAllByEntityIdAndEntityTypeAndState("test-user-id", TestingEntityType.USER,
                                                                      TestingState.ACTIVE))
            .thenReturn(Collections.emptyList());

        underTest.addTestEntityToSessionForRemoval(testingSession, "test-user-id");

        verify(jmsTemplate, never()).convertAndSend(eq(CLEANUP_USER), any(CleanupEntity.class));
        verify(testingEntityRepo).save(testingEntityArgumentCaptor.capture());

        TestingEntity testingEntity = testingEntityArgumentCaptor.getValue();
        assertEquals("test-user-id", testingEntity.getEntityId());
        assertEquals(testingSession.getId(), testingEntity.getTestingSessionId());
        assertEquals(TestingEntityType.USER, testingEntity.getEntityType());
    }

    /**
     * @verifies ignore non-active test entities
     * @see TestingUserService#addTestUserToSessionForRemoval(uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession, String)
     */
    @Test
    void addTestUserToSessionForRemoval_shouldIgnoreNonactiveTestEntities() throws Exception {
        TestingSession testingSession = new TestingSession();
        testingSession.setId(UUID.randomUUID().toString());

        TestingEntity testingEntity = new TestingEntity();
        testingEntity.setState(TestingState.REMOVE);
        testingEntity.setEntityType(TestingEntityType.USER);

        when(testingEntityRepo.findAllByEntityIdAndEntityTypeAndState("test-user-id", TestingEntityType.USER,
                                                                      TestingState.ACTIVE))
            .thenReturn(Collections.singletonList(testingEntity));

        underTest.addTestUserToSessionForRemoval(testingSession, "test-user-id");

        verify(jmsTemplate, never()).convertAndSend(eq(CLEANUP_USER), any(CleanupEntity.class));
        verify(testingEntityRepo, never()).save(any());
    }

    /**
     * @verifies remove entity before cleanup
     * @see TestingUserService#forceRemoveTestUser(String)
     */
    @Test
    void forceRemoveTestUser_shouldRemoveEntityBeforeCleanup() throws Exception {
        underTest.forceRemoveTestUser("test-user-id");
        verify(idamV2UserManagementApi).deleteUser("test-user-id");
    }

    /**
     * @verifies request cleanup of existing test entity
     * @see TestingUserService#removeTestUser(String)
     */
    @Test
    void removeTestUser_shouldRequestCleanupOfExistingTestEntity() throws Exception {
        TestingEntity testingEntity = new TestingEntity();
        testingEntity.setState(TestingState.ACTIVE);
        testingEntity.setEntityType(TestingEntityType.USER);

        when(testingEntityRepo.findAllByEntityIdAndEntityTypeAndState("test-user-id", TestingEntityType.USER,
                                                                      TestingState.ACTIVE))
            .thenReturn(Collections.singletonList(testingEntity));

        underTest.removeTestUser("test-user-id");

        verify(jmsTemplate).convertAndSend(eq(CLEANUP_USER), any(CleanupEntity.class));
        verify(testingEntityRepo, never()).save(any());
    }

    /**
     * @verifies create new burner test entity if not already present
     * @see TestingUserService#removeTestUser(String)
     */
    @Test
    void removeTestUser_shouldCreateNewBurnerTestEntityIfNotAlreadyPresent() throws Exception {
        when(testingEntityRepo.findAllByEntityIdAndEntityTypeAndState("test-user-id", TestingEntityType.USER,
                                                                      TestingState.ACTIVE))
            .thenReturn(Collections.emptyList());

        underTest.removeTestUser("test-user-id");

        verify(jmsTemplate, never()).convertAndSend(eq(CLEANUP_USER), any(CleanupEntity.class));
        verify(testingEntityRepo).save(testingEntityArgumentCaptor.capture());

        TestingEntity testingEntity = testingEntityArgumentCaptor.getValue();
        assertEquals("test-user-id", testingEntity.getEntityId());
        assertNull(testingEntity.getTestingSessionId());
        assertEquals(TestingEntityType.USER, testingEntity.getEntityType());
    }

    /**
     * @verifies delete user
     * @see TestingUserService#deleteEntity(String)
     */
    @Test
    void deleteEntity_shouldDeleteUser() throws Exception {
        underTest.deleteEntity("test-user-id");
        verify(idamV2UserManagementApi).deleteUser("test-user-id");
    }

    /**
     * @verifies get entity key
     * @see TestingUserService#getEntityKey(User)
     */
    @Test
    void getEntityKey_shouldGetEntityKey() throws Exception {
        User testUser = new User();
        testUser.setId("test-user-id");
        assertEquals("test-user-id", underTest.getEntityKey(testUser));
    }

    /**
     * @verifies get entity type
     * @see TestingUserService#getTestingEntityType()
     */
    @Test
    void getTestingEntityType_shouldGetEntityType() throws Exception {
        assertEquals(underTest.getTestingEntityType(), TestingEntityType.USER);
    }

    /**
     * @verifies return user
     * @see TestingUserService#getUserByUserId(String)
     */
    @Test
    void getUserByUserId_shouldReturnUser() throws Exception {
        User testUser = new User();
        when(idamV2UserManagementApi.getUser("test-id")).thenReturn(testUser);
        assertEquals(underTest.getUserByUserId("test-id"), testUser);
    }

    /**
     * @verifies return user
     * @see TestingUserService#getUserByEmail(String)
     */
    @Test
    void getUserByEmail_shouldReturnUser() throws Exception {
        User testUser = new User();
        when(idamV2UserManagementApi.getUserByEmail("test-email")).thenReturn(testUser);
        assertEquals(underTest.getUserByEmail("test-email"), testUser);
    }

    @Test
    void isRecentLogin_noLastLogin() {
        User testUser = new User();
        testUser.setLastLoginDate(null);
        when(idamV2UserManagementApi.getUser("test-id")).thenReturn(testUser);
        assertFalse(underTest.isRecentLogin("test-id"));
    }

    @Test
    void isRecentLogin_lastLoginIsRecent() {
        User testUser = new User();
        testUser.setLastLoginDate(ZonedDateTime.now(testClock).minusMinutes(5));
        when(idamV2UserManagementApi.getUser("test-id")).thenReturn(testUser);
        assertTrue(underTest.isRecentLogin("test-id"));
    }

    @Test
    void isRecentLogin_lastLoginNotRecent() {
        User testUser = new User();
        testUser.setLastLoginDate(ZonedDateTime.now(testClock).minusMinutes(11));
        when(idamV2UserManagementApi.getUser("test-id")).thenReturn(testUser);
        assertFalse(underTest.isRecentLogin("test-id"));
    }

    @Test
    void isRecentLogin_userNotFound() {
        when(idamV2UserManagementApi.getUser("test-id")).thenThrow(SpringWebClientHelper.notFound());
        assertFalse(underTest.isRecentLogin("test-id"));
    }

    @Test
    void testValidateProperties_invalid() {
        ReflectionTestUtils.setField(underTest, "recentLoginDuration", Duration.ofMinutes(90));
        ReflectionTestUtils.setField(underTest, "sessionLifespan", Duration.ofMinutes(30));
        underTest.validateProperties();
        Duration afterValue = (Duration) ReflectionTestUtils.getField(underTest, "recentLoginDuration");
        assertEquals(afterValue, Duration.ofMinutes(15));
    }

    @Test
    void testValidateProperties_okay() {
        ReflectionTestUtils.setField(underTest, "recentLoginDuration", Duration.ofMinutes(2));
        ReflectionTestUtils.setField(underTest, "sessionLifespan", Duration.ofMinutes(20));
        underTest.validateProperties();
        Duration afterValue = (Duration) ReflectionTestUtils.getField(underTest, "recentLoginDuration");
        assertEquals(afterValue, Duration.ofMinutes(2));
    }
}
