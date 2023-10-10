package uk.gov.hmcts.cft.idam.testingsupportapi.repo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

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
