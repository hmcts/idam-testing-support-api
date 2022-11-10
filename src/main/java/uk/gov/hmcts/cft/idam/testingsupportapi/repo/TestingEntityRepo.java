package uk.gov.hmcts.cft.idam.testingsupportapi.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;

import java.time.ZonedDateTime;
import java.util.List;

public interface TestingEntityRepo extends CrudRepository<TestingEntity, String> {

    List<TestingEntity> findByTestingSessionIdAndEntityType(String sessionId, TestingEntityType entityType);

    List<TestingEntity> findAllByEntityIdAndEntityType(String entityId, TestingEntityType entityType);

    Page<TestingEntity> findByEntityTypeAndCreateDateBeforeAndTestingSessionIdIsNullOrderByCreateDateAsc(
        TestingEntityType entityType, ZonedDateTime timestamp, Pageable pageable);

}
