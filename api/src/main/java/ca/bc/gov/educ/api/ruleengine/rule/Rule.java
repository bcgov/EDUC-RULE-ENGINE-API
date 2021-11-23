package ca.bc.gov.educ.api.ruleengine.rule;

import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.ruleengine.dto.RuleData;

@Component
public interface Rule {

    RuleData fire();

    void setInputData(RuleData inputData);
}
