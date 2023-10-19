package ca.bc.gov.educ.api.ruleengine.service;

import ca.bc.gov.educ.api.ruleengine.dto.ProgramAlgorithmRule;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.rule.Rule;
import ca.bc.gov.educ.api.ruleengine.rule.RuleFactory;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RuleEngineService {

    private static final Logger logger = LoggerFactory.getLogger(RuleEngineService.class);

    /**
     * Process all the Grad Algorithm Rules
     *
     * @return RuleProcessorData
     */
    @SneakyThrows
    public RuleProcessorData processGradAlgorithmRules(RuleProcessorData ruleProcessorData) {

        logger.debug("In Service ProcessGradAlgorithmRules");
        
        RuleProcessorData originalData = RuleProcessorRuleUtils.cloneObject(ruleProcessorData);
        ruleProcessorData.setGraduated(true);
        for (ProgramAlgorithmRule gradAlgorithmRule : originalData.getAlgorithmRules()) {
        	Rule rule = RuleFactory.createRule(gradAlgorithmRule.getAlgorithmRuleCode().getRuleImplementation(), ruleProcessorData);
            rule.setInputData(ruleProcessorData);
            ruleProcessorData = (RuleProcessorData)rule.fire();
        }
        if(ruleProcessorData.getNonGradReasons() == null || ruleProcessorData.getNonGradReasons().isEmpty()) {
            ruleProcessorData.setGraduated(ruleProcessorData.getRequirementsMet() != null && !ruleProcessorData.getRequirementsMet().isEmpty());
        }else {
            ruleProcessorData.setGraduated(false);
        }
        return ruleProcessorData;
    }

}
