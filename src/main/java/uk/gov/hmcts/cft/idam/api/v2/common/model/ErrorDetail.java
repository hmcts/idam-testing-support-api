package uk.gov.hmcts.cft.idam.api.v2.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ErrorDetail {

    private String path;
    private String code;
    private String message;

}
