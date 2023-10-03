package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper;
import uk.gov.hmcts.cft.idam.api.v2.common.model.AccountStatus;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ErrorDetail;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.error.ErrorReason;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;
import uk.gov.hmcts.cft.rd.api.RefDataUserProfileApi;
import uk.gov.hmcts.cft.rd.model.UserCategory;
import uk.gov.hmcts.cft.rd.model.UserProfile;
import uk.gov.hmcts.cft.rd.model.UserStatus;
import uk.gov.hmcts.cft.rd.model.UserType;

import java.util.Optional;

import static uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper.optionalWhenNotFound;

@Service @Slf4j public class TestingUserProfileService extends TestingEntityService<UserProfile> {

    private final RefDataUserProfileApi refDataUserProfileApi;

    private final TestingUserService testingUserService;

    protected TestingUserProfileService(RefDataUserProfileApi refDataUserProfileApi,
                                        TestingEntityRepo testingEntityRepo, JmsTemplate jmsTemplate,
                                        TestingUserService testingUserService) {
        super(testingEntityRepo, jmsTemplate);
        this.refDataUserProfileApi = refDataUserProfileApi;
        this.testingUserService = testingUserService;
    }

    public UserProfile getUserProfileByUserId(String userId) {
        return refDataUserProfileApi.getUserProfileById(userId);
    }

    public User createOrUpdateCftUser(String sessionId, User requestUser, String secretPhrase) throws Exception {
        Optional<UserProfile> existingUserProfile =
            findUserProfileForUpdate(requestUser.getId(), requestUser.getEmail());
        Optional<User> existingUser = findUserForUpdate(requestUser.getId(), requestUser.getEmail());

        User idamUser =
            existingUser.orElseGet(() -> testingUserService.createTestUser(sessionId, requestUser, secretPhrase));

        if (existingUserProfile.isEmpty()) {
            createTestUserProfile(sessionId, convertToUserProfile(idamUser));
        } else if (existingUserProfile.get().getIdamStatus() != UserStatus.ACTIVE &&
            idamUser.getAccountStatus() == AccountStatus.ACTIVE) {
            throw SpringWebClientHelper.conflict(new ErrorDetail("profile.status",
                                                                 ErrorReason.INCONSISTENT.name(),
                                                                 "user profile status is inconsistent with IDAM status"
            ));
        }

        return idamUser;
    }

    private UserProfile createTestUserProfile(String sessionId, UserProfile userProfile) {
        refDataUserProfileApi.createUserProfile(userProfile);
        createTestingEntity(sessionId, userProfile);
        return userProfile;
    }

    private Optional<UserProfile> findUserProfileForUpdate(String userId, String email) throws Exception {
        Optional<UserProfile> existingUserProfile = findUserProfileByEmail(email);
        if (existingUserProfile.isPresent()) {
            if (userId != null && !userId.equals(existingUserProfile.get().getUserIdentifier())) {
                throw SpringWebClientHelper.conflict(new ErrorDetail("user-profile.email",
                                                                     ErrorReason.NOT_UNIQUE.name(),
                                                                     "Email in use for id " +
                                                                         existingUserProfile.get().getIdamId()
                ));
            }
        } else if (userId != null) {
            existingUserProfile = findUserProfileById(userId);
            if (existingUserProfile.isPresent()) {
                throw SpringWebClientHelper.conflict(new ErrorDetail("user-profile.id",
                                                                     ErrorReason.NOT_UNIQUE.name(),
                                                                     "Id in use with email " +
                                                                         existingUserProfile.get().getEmail()
                ));
            }
        }
        return existingUserProfile;
    }

    private Optional<UserProfile> findUserProfileById(String userId) {
        return optionalWhenNotFound(() -> refDataUserProfileApi.getUserProfileById(userId));

    }

    private Optional<UserProfile> findUserProfileByEmail(String email) {
        return optionalWhenNotFound(() -> refDataUserProfileApi.getUserProfileByEmail(email));
    }

    private Optional<User> findUserForUpdate(String userId, String email) throws Exception {
        Optional<User> existingUser = findUserByEmail(email);
        if (existingUser.isPresent()) {
            if (userId != null && !userId.equals(existingUser.get().getId())) {
                throw SpringWebClientHelper.conflict(new ErrorDetail("user.email",
                                                                     ErrorReason.NOT_UNIQUE.name(),
                                                                     "Email in use for id " + existingUser.get().getId()
                ));
            }
        } else if (userId != null) {
            existingUser = findUserById(userId);
            if (existingUser.isPresent()) {
                throw SpringWebClientHelper.conflict(new ErrorDetail("user.id",
                                                                     ErrorReason.NOT_UNIQUE.name(),
                                                                     "Id in use with email " +
                                                                         existingUser.get().getEmail()
                ));
            }
        }
        return existingUser;
    }

    private Optional<User> findUserById(String userId) {
        return optionalWhenNotFound(() -> testingUserService.getUserByUserId(userId));
    }

    private Optional<User> findUserByEmail(String email) {
        return optionalWhenNotFound(() -> testingUserService.getUserByEmail(email));
    }

    private UserProfile convertToUserProfile(User user) {
        UserProfile userProfile = new UserProfile();
        userProfile.setEmail(user.getEmail());
        userProfile.setUserIdentifier(user.getId());
        userProfile.setIdamId(user.getId());
        userProfile.setFirstName(user.getForename());
        userProfile.setLastName(user.getSurname());
        userProfile.setIdamStatus(
            user.getAccountStatus() == AccountStatus.SUSPENDED ? UserStatus.SUSPENDED : UserStatus.ACTIVE);
        userProfile.setRoleNames(user.getRoleNames());
        // FUTURE determine category
        userProfile.setUserCategory(UserCategory.PROFESSIONAL);
        userProfile.setUserType(UserType.EXTERNAL);
        return userProfile;
    }

    @Override protected void deleteEntity(String key) {
        refDataUserProfileApi.deleteUserProfile(key);
    }

    @Override protected String getEntityKey(UserProfile entity) {
        return entity.getUserIdentifier() != null ? entity.getUserIdentifier() : entity.getIdamId();
    }

    @Override protected TestingEntityType getTestingEntityType() {
        return TestingEntityType.PROFILE;
    }
}
