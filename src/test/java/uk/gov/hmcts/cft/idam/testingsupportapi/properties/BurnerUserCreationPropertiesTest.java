package uk.gov.hmcts.cft.idam.testingsupportapi.properties;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
=import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BurnerUserCreationPropertiesTest {

    @Test
    public void testProperties() {
        List<String> poisonList = new ArrayList<>();
        poisonList.add("BAD-ROLE");
        poisonList.add(" ");
        BurnerUserCreationProperties underTest = new BurnerUserCreationProperties();
        underTest.setPoisonRoleNames(poisonList);
        underTest.normalize();
        assertEquals(1, underTest.getPoisonRoleNames().size());
        assertEquals("bad-role", underTest.getPoisonRoleNames().get(0));

        poisonList = new ArrayList<>();
        underTest.setPoisonRoleNames(poisonList);
        underTest.normalize();
        assertEquals(poisonList, underTest.getPoisonRoleNames());
    }

}
