package uk.gov.hmcts.cft.idam.testingsupportapi.repo.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.ZonedDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Entity
public class TestingEntity {

    @Id
    private String id;

    @NotNull
    private String entityId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TestingEntityType entityType;

    private String testingSessionId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TestingState state;

    @NotNull
    private ZonedDateTime createDate;

    private ZonedDateTime lastModifiedDate;
}
