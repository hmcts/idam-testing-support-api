package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;
import uk.gov.hmcts.cft.rd.api.RefDataCaseWorkerApi;
import uk.gov.hmcts.cft.rd.model.CaseWorkerProfile;
import uk.gov.hmcts.cft.rd.model.CreateCaseWorkerProfileRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestingCaseWorkerProfileServiceTest {

    @Mock
    RefDataCaseWorkerApi refDataCaseWorkerApi;

    @Mock
    TestingUserService testingUserService;

    @Mock
    TestingEntityRepo testingEntityRepo;

    @InjectMocks
    TestingCaseWorkerProfileService underTest;

    @Captor
    ArgumentCaptor<CreateCaseWorkerProfileRequest> createCaseWorkerProfileRequestArgumentCaptor;

    @Captor
    ArgumentCaptor<TestingEntity> testingEntityArgumentCaptor;

    @Test
    void createCaseWorkerProfile() {
        User testUser = new User();
        testUser.setId("test-user-id");
        testUser.setEmail("test-email");
        testUser.setForename("test-forename");
        testUser.setSurname("test-surname");
        testUser.setRoleNames(List.of("staff-admin"));
        underTest.createCaseWorkerProfile("test-session-id", testUser);
        verify(refDataCaseWorkerApi, times(1)).createCaseWorkerProfile(createCaseWorkerProfileRequestArgumentCaptor.capture());
        CreateCaseWorkerProfileRequest request = createCaseWorkerProfileRequestArgumentCaptor.getValue();
        assertEquals(testUser.getId(), request.getCaseWorkerId());
        assertEquals(testUser.getEmail(), request.getEmail());
        assertEquals(testUser.getForename(), request.getFirstName());
        assertEquals(testUser.getSurname(), request.getLastName());
        assertTrue(request.isStaffAdmin());

        verify(testingEntityRepo, times(1)).save(testingEntityArgumentCaptor.capture());
        TestingEntity createdTestingEntity = testingEntityArgumentCaptor.getValue();
        assertEquals("test-session-id", createdTestingEntity.getTestingSessionId());
        assertEquals("test-user-id", createdTestingEntity.getEntityId());
        assertEquals(TestingEntityType.PROFILE_CASEWORKER, createdTestingEntity.getEntityType());

    }

    @Test
    void getCaseWorkerProfileById() {
        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        when(refDataCaseWorkerApi.findCaseWorkerProfileByUserId("test-id")).thenReturn(caseWorkerProfile);
        CaseWorkerProfile result = underTest.getCaseWorkerProfileById("test-id");
        assertEquals(caseWorkerProfile, result);
    }

    @Test
    void deleteEntity() {
        underTest.deleteEntity("test-user-id");
        verify(refDataCaseWorkerApi).deleteCaseWorkerProfileByUserId("test-user-id");
    }

    @Test
    void getEntityKey() {
        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("test-id");
        assertEquals("test-id", underTest.getEntityKey(caseWorkerProfile));
    }

    @Test
    void getTestingEntityType() {
        assertEquals(underTest.getTestingEntityType(), TestingEntityType.PROFILE_CASEWORKER);
    }
}
