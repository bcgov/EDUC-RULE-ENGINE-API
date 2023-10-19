package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public interface Rule extends Serializable {

    RuleData fire();

    void setInputData(RuleData inputData);
}
