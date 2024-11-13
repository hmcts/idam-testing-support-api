package uk.gov.hmcts.cft.idam.testingsupportapi.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingState;

import java.time.ZonedDateTime;
import java.util.List;

public interface TestingSessionRepo extends CrudRepository<TestingSession, String> {

    TestingSession findFirstBySessionKeyOrderByCreateDateAsc(String sessionKey);

    List<TestingSession> findTop10ByCreateDateBeforeOrderByCreateDateAsc(ZonedDateTime timestamp);

    Page<TestingSession> findByCreateDateBeforeAndStateOrderByCreateDateAsc(ZonedDateTime timestamp, TestingState state,
                                                                            Pageable pageable);

    @Query("UPDATE TestingSession ts set ts.state=?1 where ts.state != ?1")
    @Modifying
    int updateAllSessionStates(TestingState state);

}
