package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cft.idam.api.v2.common.model.AccountStatus;
import uk.gov.hmcts.cft.idam.api.v2.common.model.ErrorDetail;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.error.ErrorReason;
import uk.gov.hmcts.cft.idam.testingsupportapi.properties.CategoryProperties;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;
import uk.gov.hmcts.cft.idam.testingsupportapi.service.model.UserProfileCategory;
import uk.gov.hmcts.cft.rd.api.RefDataUserProfileApi;
import uk.gov.hmcts.cft.rd.model.CaseWorkerProfile;
import uk.gov.hmcts.cft.rd.model.UserCategory;
import uk.gov.hmcts.cft.rd.model.UserProfile;
import uk.gov.hmcts.cft.rd.model.UserStatus;
import uk.gov.hmcts.cft.rd.model.UserType;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper.conflict;
import static uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper.optionalWhenNotFound;

@Service
@Slf4j
public class TestingUserProfileService extends TestingEntityService<UserProfile> {

    private final RefDataUserProfileApi refDataUserProfileApi;

    private final TestingUserService testingUserService;

    private final TestingCaseWorkerProfileService testingCaseWorkerProfileService;

    private final CategoryProperties categoryProperties;

    private static final String ID_IN_USE = "Id in use with email ";

    protected TestingUserProfileService(RefDataUserProfileApi refDataUserProfileApi,
                                        TestingEntityRepo testingEntityRepo,
                                        JmsTemplate jmsTemplate,
                                        TestingUserService testingUserService,
                                        TestingCaseWorkerProfileService testingCaseWorkerProfileService,
                                        CategoryProperties categoryProperties) {
        super(testingEntityRepo, jmsTemplate);
        this.refDataUserProfileApi = refDataUserProfileApi;
        this.testingUserService = testingUserService;
        this.testingCaseWorkerProfileService = testingCaseWorkerProfileService;
        this.categoryProperties = categoryProperties;
    }

    public UserProfile getUserProfileByUserId(String userId) {
        return refDataUserProfileApi.getUserProfileById(userId);
    }

    public User createOrUpdateCftUser(String sessionId, User requestUser, String secretPhrase) throws Exception {
        Optional<UserProfile> existingUserProfile = findUserProfileForUpdate(requestUser.getId(),
                                                                             requestUser.getEmail()
        );
        Optional<User> existingUser = findUserForUpdate(requestUser.getId(), requestUser.getEmail());

        User idamUser = existingUser.orElseGet(() -> testingUserService.createTestUser(sessionId,
                                                                                       requestUser,
                                                                                       secretPhrase
        ));

        Set<UserProfileCategory> categories = getUserProfileCategories(idamUser);

        if (existingUserProfile.isEmpty()) {
            createTestUserProfile(sessionId, convertToUserProfile(idamUser, categories));
        } else if (existingUserProfile.get().getIdamStatus() != UserStatus.ACTIVE
            && idamUser.getAccountStatus() == AccountStatus.ACTIVE) {
            throw conflict(new ErrorDetail("user-profile.status",
                                           ErrorReason.INCONSISTENT.name(),
                                           "user profile status is inconsistent with user status"
            ));
        } else if (!getEntityKey(existingUserProfile.get()).equals(idamUser.getId())) {
            throw conflict(new ErrorDetail("user-profile.id",
                                           ErrorReason.INCONSISTENT.name(),
                                           "user profile id " + getEntityKey(existingUserProfile.get())
                                               + " does not match user id " + idamUser.getId()
            ));
        }

        if (categories.contains(UserProfileCategory.CASEWORKER)) {
            Optional<CaseWorkerProfile> existingCaseWorkerProfile = findCaseWorkerProfileForUpdate(
                idamUser.getId(),
                idamUser.getEmail()
            );
            if (existingCaseWorkerProfile.isEmpty()) {
                testingCaseWorkerProfileService.createCaseWorkerProfile(sessionId, idamUser);
            } else if (existingCaseWorkerProfile.get().isSuspended()
                && idamUser.getAccountStatus() != AccountStatus.SUSPENDED) {
                throw conflict(new ErrorDetail("caseworker-profile.status",
                                               ErrorReason.INCONSISTENT.name(),
                                               "caseworker profile status is inconsistent with user status"
                ));
            }
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
            if (userId != null && !userId.equals(getEntityKey(existingUserProfile.get()))) {
                throw conflict(new ErrorDetail("user-profile.email",
                                               ErrorReason.NOT_UNIQUE.name(),
                                               "Email in use for id " + getEntityKey(existingUserProfile.get())
                ));
            }
        } else if (userId != null) {
            existingUserProfile = findUserProfileById(userId);
            if (existingUserProfile.isPresent()) {
                throw conflict(new ErrorDetail("user-profile.id",
                                               ErrorReason.NOT_UNIQUE.name(),
                                               ID_IN_USE + existingUserProfile.get().getEmail()
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
                throw conflict(new ErrorDetail("user.email",
                                               ErrorReason.NOT_UNIQUE.name(),
                                               "Email in use for id " + existingUser.get().getId()
                ));
            }
        } else if (userId != null) {
            existingUser = findUserById(userId);
            if (existingUser.isPresent()) {
                throw conflict(new ErrorDetail("user.id",
                                               ErrorReason.NOT_UNIQUE.name(),
                                               ID_IN_USE + existingUser.get().getEmail()
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

    private Optional<CaseWorkerProfile> findCaseWorkerProfileForUpdate(String userId, String email) throws Exception {
        Optional<CaseWorkerProfile> existingCaseworker = findCaseWorkerProfileById(userId);
        if (existingCaseworker.isPresent() && existingCaseworker.get().getEmail() != null && !existingCaseworker
            .get()
            .getEmail()
            .equalsIgnoreCase(email)) {
            throw conflict(new ErrorDetail("caseworker-profile.id",
                                           ErrorReason.NOT_UNIQUE.name(),
                                           ID_IN_USE + existingCaseworker.get().getEmail()
            ));
        }
        return existingCaseworker;
    }

    private Optional<CaseWorkerProfile> findCaseWorkerProfileById(String userId) {
        return optionalWhenNotFound(() -> testingCaseWorkerProfileService.getCaseWorkerProfileById(userId));
    }

    private UserProfile convertToUserProfile(User user, Set<UserProfileCategory> categories) {
        UserProfile userProfile = new UserProfile();
        userProfile.setEmail(user.getEmail());
        userProfile.setUserIdentifier(user.getId());
        userProfile.setIdamId(user.getId());
        userProfile.setFirstName(user.getForename());
        userProfile.setLastName(user.getSurname());
        userProfile.setIdamStatus(
            user.getAccountStatus() == AccountStatus.SUSPENDED ? UserStatus.SUSPENDED : UserStatus.ACTIVE);
        userProfile.setRoleNames(user.getRoleNames());
        if (categories.contains(UserProfileCategory.CASEWORKER)) {
            userProfile.setUserCategory(UserCategory.CASEWORKER);
        } else if (categories.contains(UserProfileCategory.PROFESSIONAL)) {
            userProfile.setUserCategory(UserCategory.PROFESSIONAL);
        }
        userProfile.setUserType(UserType.EXTERNAL);
        return userProfile;
    }

    @Override
    protected void deleteEntity(String key) {
        refDataUserProfileApi.deleteUserProfile(key);
    }

    @Override
    protected String getEntityKey(UserProfile entity) {
        return entity.getUserIdentifier() != null ? entity.getUserIdentifier() : entity.getIdamId();
    }

    @Override
    protected TestingEntityType getTestingEntityType() {
        return TestingEntityType.PROFILE;
    }

    protected Set<UserProfileCategory> getUserProfileCategories(User user) {
        return getUserProfileCategories(user.getRoleNames());
    }

    protected Set<UserProfileCategory> getUserProfileCategories(List<String> roleNames) {
        Set<UserProfileCategory> categories = new HashSet<>();
        if (CollectionUtils.isNotEmpty(roleNames)) {
            for (String roleName : roleNames) {
                if (matchesAny(roleName, categoryProperties.getRolePatterns().get("judiciary"))) {
                    categories.add(UserProfileCategory.JUDICIARY);
                } else if (matchesAny(roleName, categoryProperties.getRolePatterns().get("citizen"))) {
                    categories.add(UserProfileCategory.CITIZEN);
                } else if (matchesAny(roleName, categoryProperties.getRolePatterns().get("professional"))) {
                    categories.add(UserProfileCategory.PROFESSIONAL);
                } else if (matchesAny(roleName, categoryProperties.getRolePatterns().get("caseworker"))) {
                    categories.add(UserProfileCategory.CASEWORKER);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(categories)) {
            return categories;
        }
        return Collections.singleton(UserProfileCategory.UNKNOWN);
    }

    protected boolean matchesAny(String value, List<String> patterns) {
        return patterns.stream().anyMatch(value.toLowerCase()::matches);
    }
}
