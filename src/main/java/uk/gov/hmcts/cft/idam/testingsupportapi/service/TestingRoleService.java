package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import uk.gov.hmcts.cft.idam.api.v1.usermanagement.IdamV1UserManagementApi;
import uk.gov.hmcts.cft.idam.api.v1.usermanagement.model.User;
import uk.gov.hmcts.cft.idam.api.v2.IdamV2ConfigApi;
import uk.gov.hmcts.cft.idam.api.v2.common.model.Role;
import uk.gov.hmcts.cft.idam.testingsupportapi.receiver.model.CleanupEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;

import java.util.List;

@Service
@Slf4j
public class TestingRoleService extends TestingEntityService<Role> {

    private static final Integer ROLE_USER_SEARCH_LIMIT = 10;

    private final IdamV2ConfigApi idamV2ConfigApi;

    private final IdamV1UserManagementApi idamV1UserManagementApi;

    private final TestingUserService testingUserService;

    protected TestingRoleService(TestingEntityRepo testingEntityRepo, JmsTemplate jmsTemplate,
                                 IdamV2ConfigApi idamV2ConfigApi, IdamV1UserManagementApi idamV1UserManagementApi,
                                 TestingUserService testingUserService) {
        super(testingEntityRepo, jmsTemplate);
        this.idamV2ConfigApi = idamV2ConfigApi;
        this.idamV1UserManagementApi = idamV1UserManagementApi;
        this.testingUserService = testingUserService;
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

    @Override
    protected CleanupFailureStrategy getCleanupFailureStrategy() {
        return CleanupFailureStrategy.CUSTOM;
    }

    @Override
    protected boolean handleCleanupException(Exception hsce,
                                             CleanupFailureStrategy cleanupFailureStrategy,
                                             CleanupEntity cleanupEntity) {
        if (hsce instanceof HttpStatusCodeException
            && ((HttpStatusCodeException)hsce).getStatusCode() == HttpStatus.PRECONDITION_FAILED) {
            if (cleanupFailureStrategy == CleanupFailureStrategy.CUSTOM) {
                List<User> usersWithRole = idamV1UserManagementApi.searchUsers(
                    "(roles:" + cleanupEntity.getEntityId() + ")",
                    ROLE_USER_SEARCH_LIMIT,
                    0
                );
                if (usersWithRole != null && !usersWithRole.isEmpty()) {
                    if (usersWithRole.size() == 1) {
                        log.info(
                            "Force removing user {} linked to role {}",
                            usersWithRole.get(0).getId(),
                            cleanupEntity.getEntityId()
                        );
                        testingUserService.forceRemoveTestUser(usersWithRole.get(0).getId());
                        doCleanup(cleanupEntity, CleanupFailureStrategy.FAIL);
                        return true;
                    } else {
                        log.info(
                            "role {} is in use by {} user(s)",
                            cleanupEntity.getEntityId(),
                            usersWithRole.size() < ROLE_USER_SEARCH_LIMIT ? "" + usersWithRole.size()
                                                                          : ROLE_USER_SEARCH_LIMIT + "+"
                        );
                    }
                }
            }
            log.info(
                "Precondition failure for role {}, testing entity with id {}, session id {}",
                cleanupEntity.getEntityId(),
                cleanupEntity.getTestingEntityId(),
                cleanupEntity.getTestingSessionId()
            );
        }
        return false;
    }
}
