package uk.gov.hmcts.cft.idam.testingsupportapi.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cft.categories")
public class CategoryProperties {

    Map<String, List<String>> rolePatterns;

}
