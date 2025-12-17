package ca.bc.gov.educ.api.ruleengine.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Getter
@Setter
public class RuleEngineApiConstants {

    public static final String CORRELATION_ID = "correlationID";
    public static final String USER_NAME = "User-Name";
    public static final String REQUEST_SOURCE = "Request-Source";
    public static final String API_NAME = "EDUC-RULE-ENGINE-API";

    //API end-point Mapping constants
    public static final String API_ROOT_MAPPING = "";
    public static final String API_VERSION = "v1";
    public static final String RULE_ENGINE_API_ROOT_MAPPING = "/api/" + API_VERSION + "/rule-engine";

    //Attribute Constants
    public static final String REQUIREMENT_CODE_ATTRIBUTE = "requirementCode";

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_FORMAT = "yyyy/MM/dd";

    //Default Attribute value constants
    public static final String DEFAULT_CREATED_BY = "RuleEngineAPI";
    public static final Date DEFAULT_CREATED_TIMESTAMP = new Date();
    public static final String DEFAULT_UPDATED_BY = "RuleEngineAPI";
    public static final Date DEFAULT_UPDATED_TIMESTAMP = new Date();

    // Splunk LogHelper Enabled
    @Value("${splunk.log-helper.enabled}")
    private boolean splunkLogHelperEnabled;

    @Value("${endpoint.keycloak.token-uri}")
    private String tokenUrl;

    @Value("${enable-v2-changes}")
    private boolean enableV2Changes;
}
