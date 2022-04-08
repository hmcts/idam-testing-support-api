package uk.gov.hmcts.cft.idam.testingsupportapi.repo;

import org.springframework.data.repository.CrudRepository;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingSession;

public interface TestingSessionRepo extends CrudRepository<TestingSession, String> {

    TestingSession findBySessionKey(String sessionKey);

}
