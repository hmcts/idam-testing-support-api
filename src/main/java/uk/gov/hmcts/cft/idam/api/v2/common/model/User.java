package uk.gov.hmcts.cft.idam.api.v2.common.model;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
public class User {

    private String id;
    private String email;
    private String forename;
    private String surname;
    private String displayName;
    private List<String> roleNames;
    private String ssoId;
    private String ssoProvider;
    private AccountStatus accountStatus;
    private RecordType recordType;
    private ZonedDateTime createDate;
    private ZonedDateTime lastModified;
    private ZonedDateTime accessLockedDate;
    private ZonedDateTime lastLoginDate;

}
