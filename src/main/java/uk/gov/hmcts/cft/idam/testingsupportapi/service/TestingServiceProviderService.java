package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.springframework.jms.core.JmsTemplate;
import uk.gov.hmcts.cft.idam.api.v2.IdamV2ConfigApi;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ServiceProvider;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;

public class TestingServiceProviderService extends TestingEntityService<ServiceProvider> {

    private final IdamV2ConfigApi idamV2ConfigApi;

    protected TestingServiceProviderService(TestingEntityRepo testingEntityRepo, JmsTemplate jmsTemplate,
                                            IdamV2ConfigApi idamV2ConfigApi) {
        super(testingEntityRepo, jmsTemplate);
        this.idamV2ConfigApi = idamV2ConfigApi;
    }

    /**
     * @should Create service and testing entity
     */
    public ServiceProvider createService(String sessionId, ServiceProvider requestService) {
        ServiceProvider testService = idamV2ConfigApi.createService(requestService);
        createTestingEntity(sessionId, testService);
        return testService;
    }

    /**
     * @should delete service
     */
    @Override
    protected void deleteEntity(String key) {
        idamV2ConfigApi.deleteService(key);
    }

    /**
     * @should get entity key
     */
    @Override
    protected String getEntityKey(ServiceProvider entity) {
        return entity.getClientId();
    }

    /**
     * @should get entity type
     */
    @Override
    protected TestingEntityType getTestingEntityType() {
        return TestingEntityType.SERVICE;
    }
}
