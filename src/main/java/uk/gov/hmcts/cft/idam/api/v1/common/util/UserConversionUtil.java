package uk.gov.hmcts.cft.idam.api.v1.common.util;

import uk.gov.hmcts.cft.idam.api.v1.common.model.V1UserWithRolesIds;
import uk.gov.hmcts.cft.idam.api.v2.common.model.AccountStatus;
import uk.gov.hmcts.cft.idam.api.v2.common.model.RecordType;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;

import java.util.List;

public class UserConversionUtil {

    private UserConversionUtil() {
    }

    public static V1UserWithRolesIds convert(User input, List<String> roleIds) {
        V1UserWithRolesIds user = new V1UserWithRolesIds();
        user.setId(input.getId());
        user.setEmail(input.getEmail());
        user.setForename(input.getForename());
        user.setSurname(input.getSurname());
        user.setSsoId(input.getSsoId());
        user.setSsoProvider(input.getSsoProvider());
        user.setRoleIds(roleIds);
        user.setCreateDate(input.getCreateDate());
        user.setLastModified(input.getLastModified());
        user.setPending(false);
        if (input.getAccountStatus() == AccountStatus.ACTIVE) {
            user.setActive(true);
            user.setLocked(false);
        } else if (input.getAccountStatus() == AccountStatus.LOCKED) {
            user.setActive(true);
            user.setLocked(true);
        } else if (input.getAccountStatus() == AccountStatus.SUSPENDED) {
            user.setActive(false);
            user.setLocked(false);
        }
        user.setStale(input.getRecordType() == RecordType.ARCHIVED);

        return user;
    }

}
