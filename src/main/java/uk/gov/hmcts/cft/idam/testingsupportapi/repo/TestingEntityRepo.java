package uk.gov.hmcts.cft.idam.testingsupportapi.repo;

import org.springframework.data.repository.CrudRepository;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;

import java.time.ZonedDateTime;
import java.util.List;

public interface TestingEntityRepo extends CrudRepository<TestingEntity, String> {

    List<TestingEntity> findByTestingSessionIdAndEntityType(String sessionId, TestingEntityType entityType);

    TestingEntity findByEntityIdAndEntityType(String entityId, TestingEntityType entityType);

    List<TestingEntity> findTop10ByEntityTypeAndCreateDateBeforeAndTestingSessionIdIsNullOrderByCreateDateAsc(
        TestingEntityType entityType,
        ZonedDateTime timestamp);

}
