package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;
import uk.gov.hmcts.cft.rd.api.RefDataUserProfileApi;
import uk.gov.hmcts.cft.rd.model.UserProfile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalAnswers.returnsSecondArg;
import static org.mockito.ArgumentMatchers.any;
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
    TestingEntityRepo testingEntityRepo;

    @InjectMocks
    TestingUserProfileService underTest;

    @Captor
    ArgumentCaptor<UserProfile> userProfileArgumentCaptor;

    @Captor
    ArgumentCaptor<TestingEntity> testingEntityArgumentCaptor;

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
        when(refDataUserProfileApi.getUserProfileByEmail(any())).thenThrow(SpringWebClientHelper.notFound());
        when(refDataUserProfileApi.getUserProfileById(any())).thenThrow(SpringWebClientHelper.notFound());
        when(testingUserService.getUserByEmail(any())).thenThrow(SpringWebClientHelper.notFound());
        when(testingUserService.getUserByUserId(any())).thenThrow(SpringWebClientHelper.notFound());
        when(testingUserService.createTestUser(any(), any(), any())).then(returnsSecondArg());

        underTest.createOrUpdateCftUser("test-session-id", testUser, "test-password");

        verify(refDataUserProfileApi, times(1)).createUserProfile(userProfileArgumentCaptor.capture());
        UserProfile createdUserProfile = userProfileArgumentCaptor.getValue();
        assertEquals("test-user-id", createdUserProfile.getUserIdentifier());
        assertEquals("test-user-id", createdUserProfile.getIdamId());
        assertEquals("test-email", createdUserProfile.getEmail());

        verify(testingEntityRepo, times(1)).save(testingEntityArgumentCaptor.capture());
        TestingEntity createdTestingEntity = testingEntityArgumentCaptor.getValue();
        assertEquals("test-session-id", createdTestingEntity.getTestingSessionId());
        assertEquals("test-user-id", createdTestingEntity.getEntityId());
        assertEquals(TestingEntityType.PROFILE, createdTestingEntity.getEntityType());
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
        assertEquals(underTest.getEntityKey(testUserProfile), "test-user-identifier");
        testUserProfile.setUserIdentifier(null);
        testUserProfile.setIdamId("test-idam-id");
        assertEquals(underTest.getEntityKey(testUserProfile), "test-idam-id");
    }

    @Test
    public void getTestingEntityType_shouldGetEntityType() throws Exception {
        assertEquals(underTest.getTestingEntityType(), TestingEntityType.PROFILE);
    }
}
