package uk.gov.hmcts.cft.idam.testingsupportapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.cft.idam.testingsupportapi.controllers.AdminController;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

@SpringBootTest
public class ContextSmokeTest {

    @Autowired
    private AdminController adminController;

    @Test
    public void contextLoads() throws Exception {
        assertThat(adminController, is(notNullValue()));
    }

}
