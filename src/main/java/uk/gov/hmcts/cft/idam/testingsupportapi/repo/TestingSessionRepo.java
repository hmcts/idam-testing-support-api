package uk.gov.hmcts.cft.idam.testingsupportapi.repo;

import org.springframework.data.repository.CrudRepository;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingState;

import java.time.ZonedDateTime;
import java.util.List;

public interface TestingSessionRepo extends CrudRepository<TestingSession, String> {

    TestingSession findBySessionKey(String sessionKey);

    List<TestingSession> findTop10ByCreateDateBeforeOrderByCreateDateAsc(ZonedDateTime timestamp);

    List<TestingSession> findTop10ByCreateDateBeforeAndStateOrderByCreateDateAsc(ZonedDateTime timestamp,
                                                                                 TestingState state);

}
