package ca.bc.gov.educ.api.ruleengine.rule;

public enum RuleType {
    MIN_CREDITS ("MC"),
    MATCH ("M"),
    MIN_CREDITS_ELECTIVE ("MCE")
    ;

    private final String value;

    RuleType(String value){
        this.value = value;
    }

    public String getValue() {
        return this.name();
    }
}
