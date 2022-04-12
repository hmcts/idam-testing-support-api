package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.model.UserTestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;

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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestingUserServiceTest {

    @Mock
    IdamV0Service idamV0Service;

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
        when(idamV0Service.createTestUser(any(), eq("test-secret"))).thenReturn(testUser);
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
     * @see TestingUserService#deleteIdamUserIfPresent(uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity)
     */
    @Test
    public void deleteIdamUserIfPresent_shouldDeleteUserAndTestingEntityIfPresent() throws Exception {
        User testUser = new User();
        testUser.setId("test-user-id");

        TestingEntity testingEntity = new TestingEntity();
        testingEntity.setEntityId("test-user-id");

        when(idamV0Service.findUserById("test-user-id")).thenReturn(Optional.of(testUser));
        assertEquals(Optional.of(testUser), underTest.deleteIdamUserIfPresent(testingEntity));

        verify(idamV0Service, times(1)).deleteUser(any());
    }

    /**
     * @verifies return empty if no user
     * @see TestingUserService#deleteIdamUserIfPresent(uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity)
     */
    @Test
    public void deleteIdamUserIfPresent_shouldReturnEmptyIfNoUser() throws Exception {
        TestingEntity testingEntity = new TestingEntity();
        testingEntity.setEntityId("test-user-id");
        when(idamV0Service.findUserById("test-user-id")).thenReturn(Optional.empty());
        assertEquals(Optional.empty(), underTest.deleteIdamUserIfPresent(testingEntity));
        verify(idamV0Service, never()).deleteUser(any());
    }
}
