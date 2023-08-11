package uk.gov.hmcts.cft.idam.testingsupportapi.repo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Entity
public class TestingSession {

    @Id
    private String id;

    @NotNull
    private String sessionKey;

    @NotNull
    private String clientId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TestingState state;

    @NotNull
    private ZonedDateTime createDate;

    private ZonedDateTime lastModifiedDate;

}
