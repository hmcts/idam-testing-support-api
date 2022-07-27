package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cft.idam.api.v2.IdamV2ConfigApi;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ServiceProvider;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestingServiceProviderServiceTest {

    @Mock
    IdamV2ConfigApi idamV2ConfigApi;

    @Mock
    TestingEntityRepo testingEntityRepo;

    @InjectMocks
    TestingServiceProviderService underTest;

    @Captor
    ArgumentCaptor<TestingEntity> testingEntityArgumentCaptor;

    /**
     * @verifies Create service and testing entity
     * @see TestingServiceProviderService#createService(String, uk.gov.hmcts.cft.idam.api.v2.common.model.ServiceProvider)
     */
    @Test
    public void createService_shouldCreateServiceAndTestingEntity() throws Exception {
        ServiceProvider testService = new ServiceProvider();
        testService.setClientId("test-service-client");
        when(idamV2ConfigApi.createService(testService)).then(returnsFirstArg());
        when(testingEntityRepo.save(any())).then(returnsFirstArg());
        String sessionId = UUID.randomUUID().toString();
        ServiceProvider result = underTest.createService(sessionId, testService);
        assertEquals(result, testService);

        verify(testingEntityRepo, times(1)).save(testingEntityArgumentCaptor.capture());
        TestingEntity testingEntity = testingEntityArgumentCaptor.getValue();

        assertEquals("test-service-client", testingEntity.getEntityId());
        assertEquals(sessionId, testingEntity.getTestingSessionId());
        assertEquals(TestingEntityType.SERVICE, testingEntity.getEntityType());
        assertNotNull(testingEntity.getCreateDate());
    }

    /**
     * @verifies delete service
     * @see TestingServiceProviderService#deleteEntity(String)
     */
    @Test
    public void deleteEntity_shouldDeleteService() throws Exception {
        assertTrue(underTest.delete("test-entity-id"));
        verify(idamV2ConfigApi, times(1)).deleteService("test-entity-id");
    }

    /**
     * @verifies get entity key
     * @see TestingServiceProviderService#getEntityKey(uk.gov.hmcts.cft.idam.api.v2.common.model.ServiceProvider)
     */
    @Test
    public void getEntityKey_shouldGetEntityKey() throws Exception {
        ServiceProvider testService = new ServiceProvider();
        testService.setClientId("test-service-client");
        assertEquals("test-service-client", underTest.getEntityKey(testService));
    }

    /**
     * @verifies get entity type
     * @see TestingServiceProviderService#getTestingEntityType()
     */
    @Test
    public void getTestingEntityType_shouldGetEntityType() throws Exception {
        assertEquals(TestingEntityType.SERVICE, underTest.getTestingEntityType());
    }
}
