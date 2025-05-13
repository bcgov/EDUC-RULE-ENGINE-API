package ca.bc.gov.educ.api.ruleengine.util;

public interface PermissionsContants {
	String _PREFIX = "hasAuthority('";
	String _SUFFIX = "')";

	String RUN_RULE_ENGINE = _PREFIX + "SCOPE_RUN_RULE_ENGINE" + _SUFFIX;
}
