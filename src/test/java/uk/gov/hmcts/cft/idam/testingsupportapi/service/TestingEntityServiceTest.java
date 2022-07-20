package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
        underTest = mock(
            TestingEntityService.class,
            Mockito.withSettings()
                .useConstructor(testingEntityRepo, jmsTemplate));

    }

    @Test
    void requestCleanup() {
        TestingEntity testingEntity = new TestingEntity();
        testingEntity.setEntityId("test-user-id");
        testingEntity.setEntityType(TestingEntityType.USER);

        doCallRealMethod().when(underTest).requestCleanup(testingEntity);
        underTest.requestCleanup(testingEntity);
        verify(jmsTemplate, times(1)).convertAndSend(eq(CLEANUP_USER), any(CleanupEntity.class));
    }

    @Test
    void deleteTestingEntityById() {
        doCallRealMethod().when(underTest).deleteTestingEntityById("test-entity-id");
        underTest.deleteTestingEntityById("test-entity-id");
        verify(testingEntityRepo, times(1)).deleteById("test-entity-id");
    }

    @Test
    void getTestingEntitiesForSessionById() {
        when(underTest.getTestingEntityType()).thenReturn(TestingEntityType.USER);
        when(testingEntityRepo.findByTestingSessionIdAndEntityType("test-session-id", TestingEntityType.USER)).thenReturn(
            Collections.emptyList());
        when(underTest.getTestingEntitiesForSessionById("test-session-id")).thenCallRealMethod();
        List<?> result = underTest.getTestingEntitiesForSessionById("test-session-id");
        assertTrue(result.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void createTestingEntity() {
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

        doThrow(new RuntimeException("bad entity")).when(underTest).deleteEntity("bad-entity-id");
        try {
            underTest.delete("bad-entity-id");
            fail();
        } catch (RuntimeException re) {
            assertThat(re.getMessage(), is("bad entity"));
        }

    }
}
