package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingState;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cft.idam.testingsupportapi.receiver.CleanupReceiver.CLEANUP_CASEWORKER;
import static uk.gov.hmcts.cft.idam.testingsupportapi.receiver.CleanupReceiver.CLEANUP_PROFILE;
import static uk.gov.hmcts.cft.idam.testingsupportapi.receiver.CleanupReceiver.CLEANUP_ROLE;
import static uk.gov.hmcts.cft.idam.testingsupportapi.receiver.CleanupReceiver.CLEANUP_SERVICE;
import static uk.gov.hmcts.cft.idam.testingsupportapi.receiver.CleanupReceiver.CLEANUP_USER;

@ExtendWith(MockitoExtension.class)
class TestingEntityServiceTest {

    @Mock
    TestingEntityRepo testingEntityRepo;

    @Mock
    JmsTemplate jmsTemplate;

    TestingEntityService underTest;

    @BeforeEach
    public void initialise() {
        underTest = mock(TestingEntityService.class,
                         Mockito.withSettings().useConstructor(testingEntityRepo, jmsTemplate)
        );
    }

    @Test
    void requestCleanup_user() {
        TestingEntity testingEntity = new TestingEntity();
        testingEntity.setEntityId("test-user-id");
        testingEntity.setEntityType(TestingEntityType.USER);

        doCallRealMethod().when(underTest).requestCleanup(testingEntity);
        underTest.requestCleanup(testingEntity);
        verify(jmsTemplate, times(1)).convertAndSend(eq(CLEANUP_USER), any(CleanupEntity.class));
    }

    @Test
    void requestCleanup_role() {
        TestingEntity testingEntity = new TestingEntity();
        testingEntity.setEntityId("test-role-name");
        testingEntity.setEntityType(TestingEntityType.ROLE);

        doCallRealMethod().when(underTest).requestCleanup(testingEntity);
        underTest.requestCleanup(testingEntity);
        verify(jmsTemplate, times(1)).convertAndSend(eq(CLEANUP_ROLE), any(CleanupEntity.class));
    }

    @Test
    void requestCleanup_service() {
        TestingEntity testingEntity = new TestingEntity();
        testingEntity.setEntityId("test-service-client");
        testingEntity.setEntityType(TestingEntityType.SERVICE);

        doCallRealMethod().when(underTest).requestCleanup(testingEntity);
        underTest.requestCleanup(testingEntity);
        verify(jmsTemplate, times(1)).convertAndSend(eq(CLEANUP_SERVICE), any(CleanupEntity.class));
    }

    @Test
    void requestCleanup_userProfile() {
        TestingEntity testingEntity = new TestingEntity();
        testingEntity.setEntityId("test-user-profile-id");
        testingEntity.setEntityType(TestingEntityType.PROFILE);

        doCallRealMethod().when(underTest).requestCleanup(testingEntity);
        underTest.requestCleanup(testingEntity);
        verify(jmsTemplate, times(1)).convertAndSend(eq(CLEANUP_PROFILE), any(CleanupEntity.class));
    }

    @Test
    void requestCleanup_caseWorkerProfile() {
        TestingEntity testingEntity = new TestingEntity();
        testingEntity.setEntityId("test-caseworker-profile-id");
        testingEntity.setEntityType(TestingEntityType.PROFILE_CASEWORKER);

        doCallRealMethod().when(underTest).requestCleanup(testingEntity);
        underTest.requestCleanup(testingEntity);
        verify(jmsTemplate, times(1)).convertAndSend(eq(CLEANUP_CASEWORKER), any(CleanupEntity.class));
    }

    @Test
    void deleteTestingEntityById() {
        when(testingEntityRepo.existsById("test-entity-id")).thenReturn(true);
        doCallRealMethod().when(underTest).deleteTestingEntityById("test-entity-id");
        underTest.deleteTestingEntityById("test-entity-id");
        verify(testingEntityRepo, times(1)).deleteById("test-entity-id");
    }

    @Test
    void deleteTestingEntityByIdNotExists() {
        when(testingEntityRepo.existsById("test-entity-id")).thenReturn(false);
        doCallRealMethod().when(underTest).deleteTestingEntityById("test-entity-id");
        underTest.deleteTestingEntityById("test-entity-id");
        verify(testingEntityRepo, never()).deleteById("test-entity-id");
    }

    @Test
    void getTestingEntitiesForSessionById() {
        when(underTest.getTestingEntityType()).thenReturn(TestingEntityType.USER);
        when(testingEntityRepo.findByTestingSessionIdAndEntityTypeAndState(
            "test-session-id",
            TestingEntityType.USER,
            TestingState.ACTIVE
        )).thenReturn(Collections.emptyList());
        when(underTest.getTestingEntitiesForSessionById("test-session-id")).thenCallRealMethod();
        List<?> result = underTest.getTestingEntitiesForSessionById("test-session-id");
        assertTrue(result.isEmpty());
    }

    @Test
    void getTestingEntitiesForSessionByTestingSession() {
        TestingSession testingSession = new TestingSession();
        testingSession.setId("test-session-id");
        when(underTest.getTestingEntityType()).thenReturn(TestingEntityType.USER);
        when(underTest.getTestingEntitiesForSession(any())).thenCallRealMethod();
        when(underTest.getTestingEntitiesForSessionById("test-session-id")).thenCallRealMethod();
        List<?> result = underTest.getTestingEntitiesForSession(testingSession);
        assertTrue(result.isEmpty());
        verify(testingEntityRepo, times(1)).findByTestingSessionIdAndEntityTypeAndState(
            "test-session-id",
            TestingEntityType.USER,
            TestingState.ACTIVE
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void createTestingEntity() {
        when(underTest.buildTestingEntity(any(), any(), any())).thenCallRealMethod();
        when(underTest.createTestingEntity(eq("test-session-id"), any())).thenCallRealMethod();
        when(underTest.getEntityKey(any())).thenReturn("test-entity-id");
        when(underTest.getTestingEntityType()).thenReturn(TestingEntityType.USER);

        when(testingEntityRepo.save(any())).then(returnsFirstArg());
        User testUser = new User();
        TestingEntity testingEntity = underTest.createTestingEntity("test-session-id", testUser);
        assertThat(testingEntity.getId(), is(notNullValue()));
        assertThat(testingEntity.getEntityId(), is("test-entity-id"));
        assertThat(testingEntity.getEntityType(), is(TestingEntityType.USER));
        assertThat(testingEntity.getTestingSessionId(), is("test-session-id"));
        assertThat(testingEntity.getState(), is(TestingState.ACTIVE));
        assertThat(testingEntity.getCreateDate(), is(notNullValue()));

        verify(testingEntityRepo, times(1)).save(any());
    }

    @Test
    void delete() {
        when(underTest.delete(any())).thenCallRealMethod();

        assertTrue(underTest.delete("test-entity-id"));
        verify(underTest, times(1)).deleteEntity("test-entity-id");

        doThrow(SpringWebClientHelper.notFound()).when(underTest).deleteEntity("missing-entity-id");
        assertFalse(underTest.delete("missing-entity-id"));

        doThrow(SpringWebClientHelper.exception(HttpStatus.FORBIDDEN, new RuntimeException("bad request"))).when(
            underTest).deleteEntity("bad-entity-id");
        try {
            underTest.delete("bad-entity-id");
            fail();
        } catch (HttpClientErrorException hcce) {
            assertThat(hcce.getMessage(), is("RuntimeException; bad request"));
        }
    }

    @Test
    void addTestEntityToSessionForRemoval_existingEntity() {
        when(underTest.getTestingEntityType()).thenReturn(TestingEntityType.USER);
        doCallRealMethod().when(underTest).addTestEntityToSessionForRemoval(any(), any());
        doCallRealMethod().when(underTest).removeTestEntity(any(), any(), any());
        doCallRealMethod().when(underTest).findAllActiveByEntityId(any());

        TestingSession testingSession = new TestingSession();
        testingSession.setId("test-session-id");

        TestingEntity testingEntity = new TestingEntity();
        testingEntity.setEntityId("test-entity-id");
        testingEntity.setState(TestingState.ACTIVE);

        when(testingEntityRepo.findAllByEntityIdAndEntityTypeAndState("test-entity-id",
                                                                      TestingEntityType.USER,
                                                                      TestingState.ACTIVE
        )).thenReturn(Collections.singletonList(testingEntity));

        underTest.addTestEntityToSessionForRemoval(testingSession, "test-entity-id");

        verify(underTest).requestCleanup(testingEntity);
    }

    @Test
    void addTestEntityToSessionForRemoval_newEntity() {
        when(underTest.getTestingEntityType()).thenReturn(TestingEntityType.USER);
        doCallRealMethod().when(underTest).addTestEntityToSessionForRemoval(any(), any());
        doCallRealMethod().when(underTest).removeTestEntity(any(), any(), any());
        doCallRealMethod().when(underTest).findAllActiveByEntityId(any());

        TestingSession testingSession = new TestingSession();
        testingSession.setId("test-session-id");

        when(testingEntityRepo.findAllByEntityIdAndEntityTypeAndState("test-entity-id",
                                                                      TestingEntityType.USER,
                                                                      TestingState.ACTIVE
        )).thenReturn(Collections.emptyList());

        underTest.addTestEntityToSessionForRemoval(testingSession, "test-entity-id");

        verify(testingEntityRepo).save(any());
    }

    @Test
    void removeTestEntity_ignoreInactiveEntities() {
        when(underTest.getTestingEntityType()).thenReturn(TestingEntityType.USER);
        doCallRealMethod().when(underTest).findAllActiveByEntityId(any());
        doCallRealMethod().when(underTest).removeTestEntity(any(), any(), any());

        when(testingEntityRepo.findAllByEntityIdAndEntityTypeAndState("test-entity-id",
                                                                      TestingEntityType.USER,
                                                                      TestingState.ACTIVE
        )).thenReturn(Collections.emptyList());

        underTest.removeTestEntity("test-session-id",
                                   "test-entity-id",
                                   TestingEntityService.MissingEntityStrategy.IGNORE
        );

        verify(testingEntityRepo, never()).save(any());
    }

    @Test
    void detachEntity() {
        doCallRealMethod().when(underTest).detachEntity("test-entity-id");
        underTest.detachEntity("test-entity-id");
        verify(testingEntityRepo, times(1)).updateTestingStateById("test-entity-id", TestingState.DETACHED);
    }

}
