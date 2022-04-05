package uk.gov.hmcts.cft.idam.testingsupportapi.repo;

import org.springframework.data.repository.CrudRepository;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;

import java.util.List;

public interface TestingEntityRepo extends CrudRepository<TestingEntity, Long> {

    List<TestingEntity> findByTestingSessionId(String sessionId);

    TestingEntity findByEntityIdAndEntityType(String entityId, TestingEntityType entityType);

}
