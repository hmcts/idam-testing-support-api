package uk.gov.hmcts.cft.idam.testingsupportapi.error;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper;

@ExtendWith(MockitoExtension.class)
class ListenerErrorHandlerTest {

    @InjectMocks
    ListenerErrorHandler listenerErrorHandler;

    @Test
    void test() {
        listenerErrorHandler.handleError(SpringWebClientHelper.notFound());
        assert true;
    }


}
