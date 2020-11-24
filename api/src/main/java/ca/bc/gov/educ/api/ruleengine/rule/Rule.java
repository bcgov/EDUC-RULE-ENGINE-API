package ca.bc.gov.educ.api.ruleengine.rule;

public interface Rule {

    RuleType ruleType = null;
    
    <T> boolean fire(T parameters);
}
