package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.struct.RuleData;
import ca.bc.gov.educ.api.ruleengine.struct.RuleProcessorData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class FailedAchievementsRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(FailedAchievementsRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {
        return ruleProcessorData;
    }

    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("FailedAchievementsRule: Rule Processor Data set.");
    }
}
