package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cft.idam.api.v2.IdamV2ConfigApi;
import uk.gov.hmcts.cft.idam.api.v2.common.model.Role;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;

@Service
@Slf4j
public class TestingRoleService extends TestingEntityService<Role> {

    private final IdamV2ConfigApi idamV2ConfigApi;

    protected TestingRoleService(TestingEntityRepo testingEntityRepo, JmsTemplate jmsTemplate,
                                 IdamV2ConfigApi idamV2ConfigApi) {
        super(testingEntityRepo, jmsTemplate);
        this.idamV2ConfigApi = idamV2ConfigApi;
    }

    /**
     * @should create role and testing entity
     */
    public Role createTestRole(String sessionId, Role requestRole) {
        Role testRole = idamV2ConfigApi.createRole(requestRole);
        createTestingEntity(sessionId, testRole);
        return testRole;
    }

    /**
     * @should delete role
     */
    @Override
    protected void deleteEntity(String key) {
        idamV2ConfigApi.deleteRole(key);
    }

    /**
     * @should get entity key
     */
    @Override
    protected String getEntityKey(Role entity) {
        return entity.getName();
    }

    /**
     * @should get entity type
     */
    @Override
    protected TestingEntityType getTestingEntityType() {
        return TestingEntityType.ROLE;
    }

}
