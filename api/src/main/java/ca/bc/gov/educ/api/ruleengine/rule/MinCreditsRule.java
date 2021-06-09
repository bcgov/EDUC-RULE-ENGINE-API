package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.apache.commons.lang3.StringUtils;
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
public class MinCreditsRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(MinCreditsRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    public RuleData fire() {
        int totalCredits;
        int requiredCredits;
        logger.debug("Min Credits Rule");

        if (ruleProcessorData.getStudentCourses() == null || ruleProcessorData.getStudentCourses().isEmpty()) {
            logger.warn("!!!Empty list sent to Min Credits Rule for processing");
            return ruleProcessorData;
        }

        List<StudentCourse> studentCourses = RuleProcessorRuleUtils.getUniqueStudentCourses(
                ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());

        logger.debug("Unique Courses: " + studentCourses.size());

        List<GradProgramRule> gradProgramRules = ruleProcessorData.getGradProgramRules()
                .stream()
                .filter(gpr -> "MC".compareTo(gpr.getRequirementType()) == 0
                            && "Y".compareTo(gpr.getIsActive()) == 0
                            && "C".compareTo(gpr.getRuleCategory()) == 0)
                .collect(Collectors.toList());

        logger.debug(gradProgramRules.toString());

        for (GradProgramRule gradProgramRule : gradProgramRules) {
            requiredCredits = Integer.parseInt(gradProgramRule.getRequiredCredits().trim());
            totalCredits = studentCourses
                    .stream()
                    .mapToInt(StudentCourse::getCredits)
                    .sum();

            if (gradProgramRule.getRequiredLevel().trim().compareTo("") != 0) {
                String requiredLevel = gradProgramRule.getRequiredLevel().trim();
                totalCredits = studentCourses
                        .stream()
                        .filter(sc -> sc.getCourseLevel().contains(requiredLevel)
                        		|| (sc.getCourseCode().startsWith("CLC") && StringUtils.isBlank(sc.getCourseLevel())))
                        .mapToInt(StudentCourse::getCredits)
                        .sum();
            }
            setCoursesReqMet(studentCourses,gradProgramRule,requiredCredits);

            if (totalCredits >= requiredCredits) {
                logger.info(gradProgramRule.getRequirementName() + " Passed");
                gradProgramRule.setPassed(true);

                List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMet();

                if (reqsMet == null)
                    reqsMet = new ArrayList<>();

                reqsMet.add(new GradRequirement(gradProgramRule.getRuleCode(),
                        gradProgramRule.getRequirementName()));
                ruleProcessorData.setRequirementsMet(reqsMet);
            } else {
                logger.info(gradProgramRule.getRequirementDesc() + " Failed!");
                ruleProcessorData.setGraduated(false);

                List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasons();

                if (nonGradReasons == null)
                    nonGradReasons = new ArrayList<>();

                nonGradReasons.add(new GradRequirement(gradProgramRule.getRuleCode(),
                        gradProgramRule.getNotMetDesc()));
                ruleProcessorData.setNonGradReasons(nonGradReasons);
            }

            logger.info("Min Credits -> Required:" + requiredCredits + " Has:" + totalCredits);
        }

        logger.debug(ruleProcessorData.toString());
        ruleProcessorData.setStudentCourses(studentCourses);
        return ruleProcessorData;
    }
    
    private void setCoursesReqMet(List<StudentCourse> studentCourses, GradProgramRule gradProgramRule, int requiredCredits) {
    	//setting those course who have met this rule
        int tC=0;
        for(StudentCourse sc:studentCourses) {
        	if(sc.getCourseLevel().contains(gradProgramRule.getRequiredLevel().trim())
            		|| (sc.getCourseCode().startsWith("CLC") && StringUtils.isBlank(sc.getCourseLevel()))) {
        		tC += sc.getCredits();
        		if(tC<=requiredCredits) {
        			processReqMet(sc,gradProgramRule);
        		}else {
        			break;
        		}
        		
        	}
        }		
	}

	public void processReqMet(StudentCourse sc, GradProgramRule gradProgramRule) {
		sc.setUsed(true);
		if (sc.getGradReqMet().length() > 0) {

            sc.setGradReqMet(sc.getGradReqMet() + ", " + gradProgramRule.getRuleCode());
            sc.setGradReqMetDetail(sc.getGradReqMetDetail() + ", " + gradProgramRule.getRuleCode()
                    + " - " + gradProgramRule.getRequirementName());
        } else {
            sc.setGradReqMet(gradProgramRule.getRuleCode());
            sc.setGradReqMetDetail(gradProgramRule.getRuleCode() + " - " + gradProgramRule.getRequirementName());
        }
    }
    
    

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("MinCreditsRule: Rule Processor Data set.");
    }
}
