package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cft.idam.api.v2.IdamV2ConfigApi;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ServiceProvider;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;
import uk.gov.hmcts.cft.idam.testingsupportapi.util.SleepHelper;

import java.time.Duration;

@Service
@Slf4j
public class TestingServiceProviderService extends TestingEntityService<ServiceProvider> {

    @Value("${idam.serviceProvider.delayDuration}")
    private Duration delayDuration;

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
        SleepHelper.safeSleep(delayDuration);
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
