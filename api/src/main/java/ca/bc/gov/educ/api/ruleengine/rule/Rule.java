package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.struct.RuleData;
import org.springframework.stereotype.Component;

@Component
public interface Rule {

    RuleType ruleType = null;

    RuleData fire();

    public void setInputData(RuleData inputData);
}
