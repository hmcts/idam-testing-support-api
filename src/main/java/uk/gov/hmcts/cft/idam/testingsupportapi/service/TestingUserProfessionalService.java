package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import uk.gov.hmcts.cft.idam.api.v2.common.error.SpringWebClientHelper;
import uk.gov.hmcts.cft.idam.api.v2.common.model.AccountStatus;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.rd.api.RDUserProfileApi;
import uk.gov.hmcts.cft.rd.model.UserCategory;
import uk.gov.hmcts.cft.rd.model.UserProfile;
import uk.gov.hmcts.cft.rd.model.UserStatus;
import uk.gov.hmcts.cft.rd.model.UserType;

import java.util.Optional;

@Service
@Slf4j
public class TestingUserProfessionalService  {

    private final RDUserProfileApi rdUserProfileApi;

    private final TestingUserService testingUserService;

    public TestingUserProfessionalService(RDUserProfileApi rdUserProfileApi, TestingUserService testingUserService) {
        this.rdUserProfileApi = rdUserProfileApi;
        this.testingUserService = testingUserService;
    }

    public User createTestUser(String sessionId, User requestUser, String secretPhrase) throws Exception {

        Optional<UserProfile> existingUserProfile = findUserProfileByEmail(requestUser.getEmail());
        if (existingUserProfile.isPresent()) {
            if (requestUser.getId() != null && !requestUser.getId().equals(existingUserProfile.get().getUserIdentifier())) {
                throw SpringWebClientHelper.conflict("user profile exists for email with different id");
            }
        } else if (requestUser.getId() != null) {
            existingUserProfile = findUserProfileById(requestUser.getId());
            if (existingUserProfile.isPresent()) {
                throw SpringWebClientHelper.conflict("user profile exists for id with different email");
            }
        }

        User idamUser = null;
        if (requestUser.getId() != null) {
            Optional<User> existingUser = testingUserService.findUserById(requestUser.getId());
            if (existingUser.isPresent() && !requestUser.getEmail().equalsIgnoreCase(existingUser.get().getEmail())) {
                throw SpringWebClientHelper.conflict("user profile exists for id with different email");
            }
            idamUser = existingUser.orElse(null);
        }

        if (idamUser == null) {
            idamUser = testingUserService.createTestUser(sessionId, requestUser, secretPhrase);
        }

        requestUser.setId(idamUser.getId());

        if (existingUserProfile.isEmpty()) {
            UserProfile newProfile = createTestUserProfessional(sessionId, requestUser);
        } else if (existingUserProfile.get().getIdamStatus() != UserStatus.ACTIVE
            && idamUser.getAccountStatus() == AccountStatus.ACTIVE) {
            throw SpringWebClientHelper.conflict("user profile status is inconsistent with account status");
        }

        return requestUser;
    }

    public UserProfile createTestUserProfessional(String sessionId, User requestUser) {

        // TODO handle session and entity

        return rdUserProfileApi.createUserProfile(convert(requestUser));
    }

    private Optional<UserProfile> findUserProfileByEmail(String email) {
        try {
            return Optional.of(rdUserProfileApi.getUserProfileByEmail(email));
        } catch (HttpStatusCodeException hsce) {
            if (hsce.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw hsce;
        }
    }

    private Optional<UserProfile> findUserProfileById(String id) {
        try {
            return Optional.of(rdUserProfileApi.getUserProfileById(id));
        } catch (HttpStatusCodeException hsce) {
            if (hsce.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw hsce;
        }
    }

    private UserProfile convert(User user) {
        UserProfile userProfile = new UserProfile();
        userProfile.setEmail(user.getEmail());
        userProfile.setUserIdentifier(user.getId());
        userProfile.setFirstName(user.getForename());
        userProfile.setLastName(user.getSurname());
        userProfile.setIdamStatus(user.getAccountStatus() == AccountStatus.SUSPENDED ? UserStatus.SUSPENDED : UserStatus.ACTIVE);
        userProfile.setRoleNames(user.getRoleNames());
        userProfile.setUserCategory(UserCategory.PROFESSIONAL);
        userProfile.setUserType(UserType.EXTERNAL);
        return userProfile;
    }
}
