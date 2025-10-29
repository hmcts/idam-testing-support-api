package uk.gov.hmcts.cft.idam.testingsupportapi.properties;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties("creation.burner")
public class BurnerUserCreationProperties {

    private List<String> poisonRoleNames;

    @PostConstruct
    protected void normalize() {
        if (CollectionUtils.isNotEmpty(poisonRoleNames)) {
            poisonRoleNames.replaceAll(String::trim);
            poisonRoleNames.removeIf(String::isEmpty);
            poisonRoleNames.replaceAll(String::toLowerCase);
        }
    }
}
