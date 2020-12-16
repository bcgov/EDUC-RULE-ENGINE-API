package ca.bc.gov.educ.api.ruleengine.util;

public interface PermissionsContants {
	String _PREFIX = "#oauth2.hasAnyScope('";
	String _SUFFIX = "')";

	String RUN_RULE_ENGINE = _PREFIX + "RUN_RULE_ENGINE" + _SUFFIX;
}
