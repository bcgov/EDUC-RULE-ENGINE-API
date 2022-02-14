package ca.bc.gov.educ.api.ruleengine.util;

import java.util.Date;

public class RuleEngineApiConstants {

    private RuleEngineApiConstants () {}

    //API end-point Mapping constants
    public static final String API_ROOT_MAPPING = "";
    public static final String API_VERSION = "v1";
    public static final String RULE_ENGINE_API_ROOT_MAPPING = "/api/" + API_VERSION + "/rule-engine";

    //Attribute Constants
    public static final String REQUIREMENT_CODE_ATTRIBUTE = "requirementCode";

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    //Default Attribute value constants
    public static final String DEFAULT_CREATED_BY = "RuleEngineAPI";
    public static final Date DEFAULT_CREATED_TIMESTAMP = new Date();
    public static final String DEFAULT_UPDATED_BY = "RuleEngineAPI";
    public static final Date DEFAULT_UPDATED_TIMESTAMP = new Date();
}
