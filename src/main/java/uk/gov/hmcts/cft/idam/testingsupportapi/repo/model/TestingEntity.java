package uk.gov.hmcts.cft.idam.testingsupportapi.repo.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import uk.gov.hmcts.cft.idam.api.v2.common.jpa.PostgreSqlEnumType;

import java.time.ZonedDateTime;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Entity
@TypeDef(
    name = "pgsql_enum",
    typeClass = PostgreSqlEnumType.class
)
public class TestingEntity {

    @Id
    private String id;

    @NotNull
    private String entityId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_enum")
    private TestingEntityType entityType;

    private String testingSessionId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_enum")
    private TestingState state;

    @NotNull
    private ZonedDateTime createDate;

    private ZonedDateTime lastModifiedDate;
}
