package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class AdultStudentGradeRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(AdultStudentGradeRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {

        List<ProgramRequirement> gradProgramRules = ruleProcessorData
                .getGradProgramRules().stream().filter(gpr -> "SG".compareTo(gpr.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
                        && "Y".compareTo(gpr.getProgramRequirementCode().getActiveRequirement()) == 0)
                .collect(Collectors.toList());

        if (RuleProcessorUtils.isNotEmptyOrNull(gradProgramRules)) {
            logger.info("#Checking SG Rule");

            for (ProgramRequirement gradProgramRule : gradProgramRules) {
                logger.debug("StudentGrade:" + ruleProcessorData.getGradStudent().getStudentGrade());

                if ("AD".compareTo(ruleProcessorData.getGradStudent().getStudentGrade()) != 0) {
                    gradProgramRule.getProgramRequirementCode().setPassed(false);
                    List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasons();

                    if (nonGradReasons == null)
                        nonGradReasons = new ArrayList<>();

                    nonGradReasons.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getTraxReqNumber(), gradProgramRule.getProgramRequirementCode().getNotMetDesc(),gradProgramRule.getProgramRequirementCode().getProReqCode()));
                    ruleProcessorData.setNonGradReasons(nonGradReasons);

                    logger.debug("#SG Rule Failed");
                }
                else {
                    logger.debug("#SG Rule Passed");
                }
            }
        }

        return ruleProcessorData;
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("AdultStudentGradeRule: Rule Processor Data set.");
    }
}
