package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.model.UserTestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;
import uk.gov.hmcts.reform.idam.api.internal.model.Account;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
     * @see TestingUserService#createTestUser(java.util.UUID, uk.gov.hmcts.cft.idam.api.v2.common.model.User, String)
     */
    @Test
    public void createTestUser_shouldCreateUserAndTestingEntity() throws Exception {
        User testUser = new User();
        testUser.setId("test-user-id");
        when(idamV0Service.createTestUser(any(), eq("test-secret"))).thenReturn(testUser);
        when(testingEntityRepo.save(any())).then(returnsFirstArg());
        UUID sessionId = UUID.randomUUID();
        UserTestingEntity result = underTest.createTestUser(sessionId, testUser, "test-secret");
        assertEquals(testUser, result.getUser());
        assertEquals("test-user-id", result.getTestingEntity().getEntityId());
        assertEquals(sessionId, result.getTestingEntity().getTestingSessionId());
        assertEquals(TestingEntityType.USER, result.getTestingEntity().getEntityType());
        assertNotNull(result.getTestingEntity().getCreateDate());
    }
}
