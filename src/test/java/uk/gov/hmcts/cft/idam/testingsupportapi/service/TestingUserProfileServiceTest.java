package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper;
import uk.gov.hmcts.cft.idam.api.v2.common.model.AccountStatus;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.properties.CategoryProperties;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.model.UserProfileCategory;
import uk.gov.hmcts.cft.rd.api.RefDataUserProfileApi;
import uk.gov.hmcts.cft.rd.model.CaseWorkerProfile;
import uk.gov.hmcts.cft.rd.model.UserProfile;
import uk.gov.hmcts.cft.rd.model.UserStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.AdditionalAnswers.returnsSecondArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestingUserProfileServiceTest {

    @Mock
    RefDataUserProfileApi refDataUserProfileApi;

    @Mock
    TestingUserService testingUserService;

    @Mock
    TestingCaseWorkerProfileService testingCaseWorkerProfileService;

    @Mock
    TestingEntityRepo testingEntityRepo;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    CategoryProperties categoryProperties;

    @InjectMocks
    TestingUserProfileService underTest;

    @Captor
    ArgumentCaptor<UserProfile> userProfileArgumentCaptor;

    @Captor
    ArgumentCaptor<User> caseWorkerProfileArgumentCaptor;

    @Captor
    ArgumentCaptor<TestingEntity> testingEntityArgumentCaptor;

    @BeforeEach
    public void setup() {
        when(categoryProperties.getRolePatterns().get("judiciary")).thenReturn(List.of("judiciary"));
        when(categoryProperties.getRolePatterns().get("caseworker")).thenReturn(List.of("caseworker"));
        when(categoryProperties.getRolePatterns().get("professional")).thenReturn(List.of("pui-.*"));
        when(categoryProperties.getRolePatterns().get("citizen")).thenReturn(List.of("citizen"));
    }

    @Test
    void getUserProfileByUserId() {
        UserProfile testUserProfile = new UserProfile();
        when(refDataUserProfileApi.getUserProfileById("test-user-id")).thenReturn(testUserProfile);
        UserProfile result = underTest.getUserProfileByUserId("test-user-id");
        assertEquals(testUserProfile, result);
    }

    @Test
    void createOrUpdateCftUser_newUser() throws Exception {
        User testUser = new User();
        testUser.setId("test-user-id");
        testUser.setEmail("test-email");
        testUser.setRoleNames(List.of("caseworker"));
        when(refDataUserProfileApi.getUserProfileByEmail(any())).thenThrow(SpringWebClientHelper.notFound());
        when(refDataUserProfileApi.getUserProfileById(any())).thenThrow(SpringWebClientHelper.notFound());
        when(testingUserService.getUserByEmail(any())).thenThrow(SpringWebClientHelper.notFound());
        when(testingUserService.getUserByUserId(any())).thenThrow(SpringWebClientHelper.notFound());
        when(testingUserService.createTestUser(any(), any(), any())).then(returnsSecondArg());
        when(testingCaseWorkerProfileService.getCaseWorkerProfileById(any())).thenThrow(SpringWebClientHelper.notFound());

        underTest.createOrUpdateCftUser("test-session-id", testUser, "test-password");

        verify(refDataUserProfileApi, times(1)).createUserProfile(userProfileArgumentCaptor.capture());
        UserProfile createdUserProfile = userProfileArgumentCaptor.getValue();
        assertEquals("test-user-id", createdUserProfile.getUserIdentifier());
        assertEquals("test-user-id", createdUserProfile.getIdamId());
        assertEquals("test-email", createdUserProfile.getEmail());

        verify(testingCaseWorkerProfileService, times(1)).createCaseWorkerProfile(
            any(),
            caseWorkerProfileArgumentCaptor.capture()
        );
        User createdCaseworkerProfile = caseWorkerProfileArgumentCaptor.getValue();
        assertEquals("test-user-id", createdCaseworkerProfile.getId());
        assertEquals("test-email", createdUserProfile.getEmail());

        verify(testingEntityRepo, times(1)).save(testingEntityArgumentCaptor.capture());
        TestingEntity createdTestingEntity = testingEntityArgumentCaptor.getValue();
        assertEquals("test-session-id", createdTestingEntity.getTestingSessionId());
        assertEquals("test-user-id", createdTestingEntity.getEntityId());
        assertEquals(TestingEntityType.PROFILE, createdTestingEntity.getEntityType());

    }

    @Test
    void createOrUpdateCftUser_conflictWithProfileEmail() throws Exception {
        UserProfile testUserProfile = new UserProfile();
        testUserProfile.setEmail("test-email");
        testUserProfile.setIdamId(("test-up-id"));
        User testUser = new User();
        testUser.setId("test-user-id");
        testUser.setEmail("test-email");
        when(refDataUserProfileApi.getUserProfileByEmail(any())).thenReturn(testUserProfile);

        try {
            underTest.createOrUpdateCftUser("test-session-id", testUser, "test-password");
            fail();
        } catch (HttpStatusCodeException hsce) {
            assertEquals(HttpStatus.CONFLICT, hsce.getStatusCode());
            assertEquals(">>>(1/1) Email in use for id test-up-id", hsce.getMessage());
        }

        verify(testingUserService, never()).createTestUser(any(), any(), any());
    }

    @Test
    void createOrUpdateCftUser_conflictWithProfileId() throws Exception {
        UserProfile testUserProfile = new UserProfile();
        testUserProfile.setEmail("test-other-email");
        testUserProfile.setIdamId(("test-up-id"));
        User testUser = new User();
        testUser.setId("test-user-id");
        testUser.setEmail("test-email");
        when(refDataUserProfileApi.getUserProfileByEmail(any())).thenThrow(SpringWebClientHelper.notFound());
        when(refDataUserProfileApi.getUserProfileById(any())).thenReturn(testUserProfile);

        try {
            underTest.createOrUpdateCftUser("test-session-id", testUser, "test-password");
            fail();
        } catch (HttpStatusCodeException hsce) {
            assertEquals(HttpStatus.CONFLICT, hsce.getStatusCode());
            assertEquals(">>>(1/1) Id in use with email test-other-email", hsce.getMessage());
        }

        verify(testingUserService, never()).createTestUser(any(), any(), any());
    }

    @Test
    void createOrUpdateCftUser_conflictWithUserEmail() throws Exception {
        User existingUser = new User();
        existingUser.setId("test-other-id");
        existingUser.setEmail("test-email");
        User testUser = new User();
        testUser.setId("test-user-id");
        testUser.setEmail("test-email");
        when(refDataUserProfileApi.getUserProfileByEmail(any())).thenThrow(SpringWebClientHelper.notFound());
        when(refDataUserProfileApi.getUserProfileById(any())).thenThrow(SpringWebClientHelper.notFound());
        when(testingUserService.getUserByEmail(any())).thenReturn(existingUser);
        try {
            underTest.createOrUpdateCftUser("test-session-id", testUser, "test-password");
            fail();
        } catch (HttpStatusCodeException hsce) {
            assertEquals(HttpStatus.CONFLICT, hsce.getStatusCode());
            assertEquals(">>>(1/1) Email in use for id test-other-id", hsce.getMessage());
        }

        verify(testingUserService, never()).createTestUser(any(), any(), any());
    }

    @Test
    void createOrUpdateCftUser_conflictWithUserId() throws Exception {
        User existingUser = new User();
        existingUser.setId("test-id");
        existingUser.setEmail("test-other-email");
        User testUser = new User();
        testUser.setId("test-user-id");
        testUser.setEmail("test-email");
        when(refDataUserProfileApi.getUserProfileByEmail(any())).thenThrow(SpringWebClientHelper.notFound());
        when(refDataUserProfileApi.getUserProfileById(any())).thenThrow(SpringWebClientHelper.notFound());
        when(testingUserService.getUserByEmail(any())).thenThrow(SpringWebClientHelper.notFound());
        when(testingUserService.getUserByUserId(any())).thenReturn(existingUser);
        try {
            underTest.createOrUpdateCftUser("test-session-id", testUser, "test-password");
            fail();
        } catch (HttpStatusCodeException hsce) {
            assertEquals(HttpStatus.CONFLICT, hsce.getStatusCode());
            assertEquals(">>>(1/1) Id in use with email test-other-email", hsce.getMessage());
        }

        verify(testingUserService, never()).createTestUser(any(), any(), any());
    }

    @Test
    void createOrUpdateCftUser_createProfileFromUser() throws Exception {
        User existingUser = new User();
        existingUser.setId("test-existing-id");
        existingUser.setEmail("test-email");
        existingUser.setForename("test-existing-forename");
        User testUser = new User();
        testUser.setId(null);
        testUser.setEmail("test-email");
        testUser.setForename("test-forename");
        when(refDataUserProfileApi.getUserProfileByEmail(any())).thenThrow(SpringWebClientHelper.notFound());
        when(testingUserService.getUserByEmail(any())).thenReturn(existingUser);

        underTest.createOrUpdateCftUser("test-session-id", testUser, "test-password");

        verify(testingUserService, never()).createTestUser(any(), any(), any());

        verify(refDataUserProfileApi, times(1)).createUserProfile(userProfileArgumentCaptor.capture());
        UserProfile createdUserProfile = userProfileArgumentCaptor.getValue();
        assertEquals("test-existing-id", createdUserProfile.getUserIdentifier());
        assertEquals("test-existing-id", createdUserProfile.getIdamId());
        assertEquals("test-email", createdUserProfile.getEmail());
        assertEquals("test-existing-forename", createdUserProfile.getFirstName());

        verify(testingEntityRepo, times(1)).save(testingEntityArgumentCaptor.capture());
        TestingEntity createdTestingEntity = testingEntityArgumentCaptor.getValue();
        assertEquals("test-session-id", createdTestingEntity.getTestingSessionId());
        assertEquals("test-existing-id", createdTestingEntity.getEntityId());
        assertEquals(TestingEntityType.PROFILE, createdTestingEntity.getEntityType());
    }

    @Test
    void createOrUpdateCftUser_createUserAndProfileConsistent() throws Exception {
        User existingUser = new User();
        existingUser.setId("test-existing-id");
        existingUser.setEmail("test-email");
        existingUser.setForename("test-existing-forename");
        existingUser.setAccountStatus(AccountStatus.SUSPENDED);

        UserProfile existingUserProfile = new UserProfile();
        existingUserProfile.setEmail("test-email");
        existingUserProfile.setIdamId(("test-up-id"));
        existingUserProfile.setIdamStatus(UserStatus.SUSPENDED);

        User testUser = new User();
        testUser.setId(null);
        testUser.setEmail("test-email");
        testUser.setForename("test-forename");

        when(refDataUserProfileApi.getUserProfileByEmail(any())).thenThrow(SpringWebClientHelper.notFound());
        when(testingUserService.getUserByEmail(any())).thenReturn(existingUser);

        underTest.createOrUpdateCftUser("test-session-id", testUser, "test-password");

        verify(testingUserService, never()).createTestUser(any(), any(), any());

        verify(refDataUserProfileApi, times(1)).createUserProfile(userProfileArgumentCaptor.capture());
        UserProfile createdUserProfile = userProfileArgumentCaptor.getValue();
        assertEquals("test-existing-id", createdUserProfile.getUserIdentifier());
        assertEquals("test-existing-id", createdUserProfile.getIdamId());
        assertEquals("test-email", createdUserProfile.getEmail());
        assertEquals("test-existing-forename", createdUserProfile.getFirstName());

        verify(testingEntityRepo, times(1)).save(testingEntityArgumentCaptor.capture());
        TestingEntity createdTestingEntity = testingEntityArgumentCaptor.getValue();
        assertEquals("test-session-id", createdTestingEntity.getTestingSessionId());
        assertEquals("test-existing-id", createdTestingEntity.getEntityId());
        assertEquals(TestingEntityType.PROFILE, createdTestingEntity.getEntityType());
    }

    @Test
    void createOrUpdateCftUser_userAndProfileInconsistentIds() throws Exception {
        User existingUser = new User();
        existingUser.setId("test-existing-id");
        existingUser.setEmail("test-email");
        existingUser.setForename("test-existing-forename");
        existingUser.setAccountStatus(AccountStatus.SUSPENDED);

        UserProfile existingUserProfile = new UserProfile();
        existingUserProfile.setEmail("test-email");
        existingUserProfile.setIdamId(("test-up-id"));
        existingUserProfile.setIdamStatus(UserStatus.SUSPENDED);

        User testUser = new User();
        testUser.setId(null);
        testUser.setEmail("test-email");
        testUser.setForename("test-forename");

        when(refDataUserProfileApi.getUserProfileByEmail(any())).thenReturn(existingUserProfile);
        when(testingUserService.getUserByEmail(any())).thenReturn(existingUser);

        try {
            underTest.createOrUpdateCftUser("test-session-id", testUser, "test-password");
            fail();
        } catch (HttpStatusCodeException hsce) {
            assertEquals(HttpStatus.CONFLICT, hsce.getStatusCode());
            assertEquals(">>>(1/1) user profile id test-up-id does not match user id test-existing-id",
                         hsce.getMessage()
            );
        }

        verify(testingUserService, never()).createTestUser(any(), any(), any());
    }

    @Test
    void createOrUpdateCftUser_userAndProfileInconsistentStatus() throws Exception {
        User existingUser = new User();
        existingUser.setId("test-existing-id");
        existingUser.setEmail("test-email");
        existingUser.setForename("test-existing-forename");
        existingUser.setAccountStatus(AccountStatus.ACTIVE);

        UserProfile existingUserProfile = new UserProfile();
        existingUserProfile.setEmail("test-email");
        existingUserProfile.setIdamId(("test-existing-id"));
        existingUserProfile.setIdamStatus(UserStatus.SUSPENDED);

        User testUser = new User();
        testUser.setId(null);
        testUser.setEmail("test-email");
        testUser.setForename("test-forename");

        when(refDataUserProfileApi.getUserProfileByEmail(any())).thenReturn(existingUserProfile);
        when(testingUserService.getUserByEmail(any())).thenReturn(existingUser);

        try {
            underTest.createOrUpdateCftUser("test-session-id", testUser, "test-password");
            fail();
        } catch (HttpStatusCodeException hsce) {
            assertEquals(HttpStatus.CONFLICT, hsce.getStatusCode());
            assertEquals(">>>(1/1) user profile status is inconsistent with user status", hsce.getMessage());
        }

        verify(testingUserService, never()).createTestUser(any(), any(), any());
    }

    @Test
    void createOrUpdateCftUser_conflictWithCaseWorkerProfileId() throws Exception {
        UserProfile testUserProfile = new UserProfile();
        testUserProfile.setEmail("test-email");
        testUserProfile.setIdamId(("test-user-id"));
        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("test-user-id");
        caseWorkerProfile.setEmail("test-other-email");
        User testUser = new User();
        testUser.setId("test-user-id");
        testUser.setEmail("test-email");
        testUser.setRoleNames(List.of("caseworker"));
        when(refDataUserProfileApi.getUserProfileByEmail(any())).thenReturn(testUserProfile);
        when(testingUserService.getUserByEmail(any())).thenReturn(testUser);
        when(testingCaseWorkerProfileService.getCaseWorkerProfileById(any())).thenReturn(caseWorkerProfile);

        try {
            underTest.createOrUpdateCftUser("test-session-id", testUser, "test-password");
            fail();
        } catch (HttpStatusCodeException hsce) {
            assertEquals(HttpStatus.CONFLICT, hsce.getStatusCode());
            assertEquals(">>>(1/1) Id in use with email test-other-email", hsce.getMessage());
        }

        verify(testingUserService, never()).createTestUser(any(), any(), any());
    }

    @Test
    void deleteEntity() {
        underTest.deleteEntity("test-user-id");
        verify(refDataUserProfileApi).deleteUserProfile("test-user-id");
    }

    @Test
    void getEntityKey() {
        UserProfile testUserProfile = new UserProfile();
        testUserProfile.setUserIdentifier("test-user-identifier");
        assertEquals("test-user-identifier", underTest.getEntityKey(testUserProfile));
        testUserProfile.setUserIdentifier(null);
        testUserProfile.setIdamId("test-idam-id");
        assertEquals("test-idam-id", underTest.getEntityKey(testUserProfile));
    }

    @Test
    void getTestingEntityType_shouldGetEntityType() throws Exception {
        assertEquals(underTest.getTestingEntityType(), TestingEntityType.PROFILE);
    }

    @Test
    void getUserProfileCategories() {
        User user = new User();
        user.setRoleNames(Arrays.asList("citizen", "caseworker", "judiciary", "pui-role", "other-role"));
        Set<UserProfileCategory> result = underTest.getUserProfileCategories(user);
        assertEquals(4, result.size());
        assertTrue(result.stream().anyMatch(s -> s == UserProfileCategory.CITIZEN));
        assertTrue(result.stream().anyMatch(s -> s == UserProfileCategory.CASEWORKER));
        assertTrue(result.stream().anyMatch(s -> s == UserProfileCategory.PROFESSIONAL));
        assertTrue(result.stream().anyMatch(s -> s == UserProfileCategory.JUDICIARY));
    }

    @Test
    void getUserProfileCategories_noRoles() {
        User user = new User();
        Set<UserProfileCategory> result = underTest.getUserProfileCategories(user);
        assertEquals(1, result.size());
        assertTrue(result.stream().anyMatch(s -> s == UserProfileCategory.UNKNOWN));
    }
}
