package uk.gov.hmcts.cft.idam.testingsupportapi.service;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cft.idam.api.v2.common.model.User;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.TestingEntityRepo;
import uk.gov.hmcts.cft.idam.testingsupportapi.repo.model.TestingEntityType;
import uk.gov.hmcts.cft.rd.api.RefDataCaseWorkerApi;
import uk.gov.hmcts.cft.rd.model.CaseWorkerLocation;
import uk.gov.hmcts.cft.rd.model.CaseWorkerProfile;
import uk.gov.hmcts.cft.rd.model.CaseWorkerRole;
import uk.gov.hmcts.cft.rd.model.CaseWorkerService;
import uk.gov.hmcts.cft.rd.model.CreateCaseWorkerProfileRequest;

import java.util.Collections;
import java.util.List;

@Service
public class TestingCaseWorkerProfileService extends TestingEntityService<CaseWorkerProfile> {

    private final RefDataCaseWorkerApi refDataCaseWorkerApi;

    public TestingCaseWorkerProfileService(TestingEntityRepo testingEntityRepo,
                                           JmsTemplate jmsTemplate,
                                           RefDataCaseWorkerApi refDataCaseWorkerApi) {
        super(testingEntityRepo, jmsTemplate);
        this.refDataCaseWorkerApi = refDataCaseWorkerApi;
    }

    public User createCaseWorkerProfile(String sessionId, User user) {
        CreateCaseWorkerProfileRequest createRequest = convertToCreateRequest(user);
        refDataCaseWorkerApi.createCaseWorkerProfile(createRequest);

        createTestingEntity(sessionId, createRequest);

        return user;
    }

    private CreateCaseWorkerProfileRequest convertToCreateRequest(User user) {
        CreateCaseWorkerProfileRequest createRequest = new CreateCaseWorkerProfileRequest();
        createRequest.setCaseWorkerId(user.getId());
        createRequest.setFirstName(user.getForename());
        createRequest.setLastName(user.getSurname());
        createRequest.setEmail(user.getEmail());

        createRequest.setSkills(Collections.emptyList());
        createRequest.setUserType("CTSC");
        createRequest.setUserProfileIdamStatus("ACTIVE");
        createRequest.setStaffAdmin(user.getRoleNames().contains("staff-admin"));
        createRequest.setRegionId("4");
        createRequest.setRegion("North East");

        CaseWorkerRole role = new CaseWorkerRole();
        role.setRoleId("2");
        role.setRoleDescription("Legal Caseworker");
        role.setPrimary(true);
        createRequest.setRoles(List.of(role));

        CaseWorkerLocation location = new CaseWorkerLocation();
        location.setLocationId("206150");
        location.setLocationDescription("Ayr Social Security and Child Support Tribunal");
        location.setPrimary(true);
        createRequest.setBaseLocations(List.of(location));

        CaseWorkerService service = new CaseWorkerService();
        service.setServiceCode("BAB2");
        createRequest.setServices(List.of(service));

        return createRequest;
    }

    public CaseWorkerProfile getCaseWorkerProfileById(String userId) {
        return refDataCaseWorkerApi.findCaseWorkerProfileByUserId(userId);
    }

    @Override
    protected void deleteEntity(String key) {
        refDataCaseWorkerApi.deleteCaseWorkerProfileByUserId(key);
    }

    @Override
    protected String getEntityKey(CaseWorkerProfile entity) {
        return entity.getCaseWorkerId();
    }

    @Override
    protected TestingEntityType getTestingEntityType() {
        return TestingEntityType.PROFILE_CASEWORKER;
    }

}
