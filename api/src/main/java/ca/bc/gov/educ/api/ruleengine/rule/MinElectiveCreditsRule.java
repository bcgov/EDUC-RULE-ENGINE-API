package ca.bc.gov.educ.api.ruleengine.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.ruleengine.struct.GradProgramRule;
import ca.bc.gov.educ.api.ruleengine.struct.GradRequirement;
import ca.bc.gov.educ.api.ruleengine.struct.RuleData;
import ca.bc.gov.educ.api.ruleengine.struct.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.struct.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class MinElectiveCreditsRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(MinElectiveCreditsRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    final RuleType ruleType = RuleType.MIN_CREDITS_ELECTIVE;

    public RuleData fire() {
        int totalCredits = 0;
        int requiredCredits = 0;
        logger.debug("Min Elective Credits Rule");

        if (ruleProcessorData.getStudentCourses() == null || ruleProcessorData.getStudentCourses().size() == 0) {
            logger.warn("!!!Empty list sent to Min Elective Credits Rule for processing");
            return null;
        }

        List<StudentCourse> studentCourses = RuleProcessorRuleUtils.getUniqueStudentCourses(
                ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());

        logger.debug("Unique Courses: " + studentCourses.size());

        List<GradProgramRule> gradProgramRules = ruleProcessorData.getGradProgramRules()
                .stream()
                .filter(gpr -> "MCE".compareTo(gpr.getRequirementType()) == 0
                        && "Y".compareTo(gpr.getIsActive()) == 0
                        && "C".compareTo(gpr.getRuleCategory()) == 0)
                .collect(Collectors.toList());

        logger.debug(gradProgramRules.toString());

        for (GradProgramRule gradProgramRule : gradProgramRules) {
            requiredCredits = Integer.parseInt(gradProgramRule.getRequiredCredits().trim()); //list

            List<StudentCourse> tempStudentCourseList = new ArrayList<>();

            if (gradProgramRule.getRequiredLevel() == null
                    || gradProgramRule.getRequiredLevel().trim().compareTo("") == 0) {
                tempStudentCourseList = studentCourses
                        .stream()
                        .filter(sc -> !sc.isUsed())
                        .collect(Collectors.toList());
            } else {
                tempStudentCourseList = studentCourses
                        .stream()
                        .filter(sc -> !sc.isUsed() && sc.getCourseLevel()
                                .compareTo(gradProgramRule.getRequiredLevel().trim()) == 0)
                        .collect(Collectors.toList());
            }

            for (StudentCourse sc : tempStudentCourseList) {
                if (totalCredits + sc.getCredits() <= requiredCredits) {
                    totalCredits += sc.getCredits();
                    sc.setCreditsUsedForGrad(sc.getCredits());
                } else {
                    int extraCredits = totalCredits + sc.getCredits() - requiredCredits;
                    totalCredits = requiredCredits;
                    sc.setCreditsUsedForGrad(sc.getCredits() - extraCredits);
                }
                if (sc.getGradReqMet().length() > 0) {
                	
                    sc.setGradReqMet(sc.getGradReqMet() + ", " + gradProgramRule.getRuleCode());
                    sc.setGradReqMetDetail(sc.getGradReqMetDetail() + ", " + gradProgramRule.getRuleCode()
                            + " - " + gradProgramRule.getRequirementName());
                } else {
                    sc.setGradReqMet(gradProgramRule.getRuleCode());
                    sc.setGradReqMetDetail(gradProgramRule.getRuleCode() + " - " + gradProgramRule.getRequirementName());
                }
                sc.setUsed(true);

                if (totalCredits == requiredCredits) {
                    break;
                }
            }

            if (totalCredits >= requiredCredits) {
                logger.info(gradProgramRule.getRequirementName() + " Passed");
                gradProgramRule.setPassed(true);

                List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMet();

                if (reqsMet == null)
                    reqsMet = new ArrayList<GradRequirement>();

                reqsMet.add(new GradRequirement(gradProgramRule.getRuleCode(),
                        gradProgramRule.getRequirementName()));
                ruleProcessorData.setRequirementsMet(reqsMet);
                logger.debug("Min Elective Credits Rule: Total-" + totalCredits + " Required-" + requiredCredits);

            } else {
                logger.info(gradProgramRule.getRequirementDesc() + " Failed!");
                ruleProcessorData.setGraduated(false);

                List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasons();

                if (nonGradReasons == null)
                    nonGradReasons = new ArrayList<GradRequirement>();

                nonGradReasons.add(new GradRequirement(gradProgramRule.getRuleCode(),
                        gradProgramRule.getNotMetDesc()));
                ruleProcessorData.setNonGradReasons(nonGradReasons);
            }

            logger.info("Min Elective Credits -> Required:" + requiredCredits + " Has:" + totalCredits);

            requiredCredits = 0;
            totalCredits = 0;
        }

        return ruleProcessorData;
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("MinElectiveCreditsRule: Rule Processor Data set.");
    }

}
