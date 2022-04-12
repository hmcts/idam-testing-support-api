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
public class TestingSession {

    @Id
    private String id;

    @NotNull
    private String sessionKey;

    @NotNull
    private String clientId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_enum")
    private TestingSessionState state;

    @NotNull
    private ZonedDateTime createDate;

    private ZonedDateTime lastModifiedDate;

}
