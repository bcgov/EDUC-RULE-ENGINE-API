package ca.bc.gov.educ.api.ruleengine.service;

import ca.bc.gov.educ.api.ruleengine.dto.ProgramAlgorithmRule;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.rule.Rule;
import ca.bc.gov.educ.api.ruleengine.rule.RuleFactory;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
            List<StudentCourse> beforeRuleCourses = ruleProcessorData.getStudentCourses();
            ruleProcessorData = (RuleProcessorData)rule.fire();
            List<StudentCourse> afterRuleCourses = ruleProcessorData.getStudentCourses();
            List<StudentCourse> courseDifference = beforeRuleCourses.stream().filter(element -> !afterRuleCourses.contains(element)).collect(Collectors.toList());
            if(logger.isDebugEnabled() && !courseDifference.isEmpty()) {
                logger.debug("The course excluded by the {}/{} rule: {}", gradAlgorithmRule.getAlgorithmRuleCode().getAlgoRuleCode(), gradAlgorithmRule.getAlgorithmRuleCode().getRuleImplementation(), StringUtils.join(courseDifference, ","));
            }
        }
        if(ruleProcessorData.getNonGradReasons() == null || ruleProcessorData.getNonGradReasons().isEmpty()) {
            ruleProcessorData.setGraduated(ruleProcessorData.getRequirementsMet() != null && !ruleProcessorData.getRequirementsMet().isEmpty());
        }else {
            ruleProcessorData.setGraduated(false);
        }
        return ruleProcessorData;
    }

}
