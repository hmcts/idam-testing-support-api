package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cft.idam.api.v2.IdamV2UserManagementApi;
import uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.model.UserTestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        UserTestingEntity result = underTest.createTestUser(sessionId, testUser, "test-secret");
        assertEquals(testUser, result.getUser());
        assertEquals("test-user-id", result.getTestingEntity().getEntityId());
        assertEquals(sessionId, result.getTestingEntity().getTestingSessionId());
        assertEquals(TestingEntityType.USER, result.getTestingEntity().getEntityType());
        assertNotNull(result.getTestingEntity().getCreateDate());
    }

    /**
     * @verifies get expired burner users
     * @see TestingUserService#getExpiredBurnerUserTestingEntities(java.time.ZonedDateTime)
     */
    @Test
    public void getExpiredBurnerUserTestingEntities_shouldGetExpiredBurnerUsers() throws Exception {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        TestingEntity testingEntity = new TestingEntity();
        when(
            testingEntityRepo
                .findTop10ByEntityTypeAndCreateDateBeforeAndTestingSessionIdIsNullOrderByCreateDateAsc(any(), any()))
            .thenReturn(Collections.singletonList(testingEntity));
        List<TestingEntity> result = underTest.getExpiredBurnerUserTestingEntities(zonedDateTime);
        assertEquals(testingEntity, result.get(0));
    }

    /**
     * @verifies delete user and testing entity if present
     * @see TestingUserService#deleteIdamUserIfPresent(String userId)
     */
    @Test
    public void deleteIdamUserIfPresent_shouldDeleteUserAndTestingEntityIfPresent() throws Exception {
        User testUser = new User();
        testUser.setId("test-user-id");

        TestingEntity testingEntity = new TestingEntity();
        testingEntity.setEntityId("test-user-id");

        when(idamV2UserManagementApi.deleteUser(eq("test-user-id"))).thenReturn(testUser);
        assertEquals(Optional.of(testUser), underTest.deleteIdamUserIfPresent("test-user-id"));

        verify(idamV2UserManagementApi, times(1)).deleteUser(eq("test-user-id"));
    }

    /**
     * @verifies return empty if no user
     * @see TestingUserService#deleteIdamUserIfPresent(String userId)
     */
    @Test
    public void deleteIdamUserIfPresent_shouldReturnEmptyIfNoUser() throws Exception {
        TestingEntity testingEntity = new TestingEntity();
        testingEntity.setEntityId("test-user-id");
        when(idamV2UserManagementApi.deleteUser(eq("test-user-id"))).thenThrow(SpringWebClientHelper.notFound());
        assertEquals(Optional.empty(), underTest.deleteIdamUserIfPresent("test-user-id"));
    }

    /**
     * @verifies get users for session
     * @see TestingUserService#getUsersForSession(uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession)
     */
    @Test
    public void getUsersForSession_shouldGetUsersForSession() throws Exception {
        TestingSession testngSession = new TestingSession();
        testngSession.setId("test-session-id");
        TestingEntity testingEntity = new TestingEntity();
        when(testingEntityRepo.findByTestingSessionId(eq("test-session-id"))).thenReturn(Collections.singletonList(testingEntity));
        List<TestingEntity> result = underTest.getUsersForSession(testngSession);
        assertEquals(result.get(0), testingEntity);
    }
}
