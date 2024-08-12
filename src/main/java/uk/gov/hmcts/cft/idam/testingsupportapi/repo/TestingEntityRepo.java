package uk.gov.hmcts.cft.idam.testingsupportapi.repo;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntity;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingState;

import java.time.ZonedDateTime;
import java.util.List;

public interface TestingEntityRepo extends CrudRepository<TestingEntity, String> {

    List<TestingEntity> findByTestingSessionIdAndEntityTypeAndState(String sessionId, TestingEntityType entityType,
                                                                    TestingState testingState);

    List<TestingEntity> findAllByEntityIdAndEntityTypeAndState(String entityId, TestingEntityType entityType,
                                                               TestingState testingState);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0"))
    Page<TestingEntity> findByEntityTypeAndCreateDateBeforeAndTestingSessionIdIsNullOrderByCreateDateAsc(
        TestingEntityType entityType, ZonedDateTime timestamp, Pageable pageable);

    @Query("UPDATE TestingEntity te set te.state=?2 where te.id = ?1")
    @Modifying
    int updateTestingStateById(String id, TestingState state);

}
