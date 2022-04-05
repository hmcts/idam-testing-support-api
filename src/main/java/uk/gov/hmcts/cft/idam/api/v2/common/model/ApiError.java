package uk.gov.hmcts.cft.idam.api.v2.common.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class ApiError {

    private Instant timestamp;
    private Integer status;
    private String method;
    private String path;
    private List<String> errors;

}
