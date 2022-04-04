package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
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
public class MinCredit1986Rule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(MinCredit1986Rule.class);

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
        List<StudentCourse> tempStudentCourseList = RuleProcessorRuleUtils.getUniqueStudentCourses(
                ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());
        List<StudentCourse> studentCourses = tempStudentCourseList.stream().filter(sc -> !sc.isUsedInMatchRule()).collect(Collectors.toList());
        logger.debug("Unique Courses: {}",studentCourses.size());

        List<ProgramRequirement> gradProgramRules = ruleProcessorData.getGradProgramRules()
                .stream()
                .filter(gpr -> "MC".compareTo(gpr.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
                            && "Y".compareTo(gpr.getProgramRequirementCode().getActiveRequirement()) == 0
                            && "C".compareTo(gpr.getProgramRequirementCode().getRequirementCategory()) == 0)
                .collect(Collectors.toList());

        logger.debug(gradProgramRules.toString());

        for (ProgramRequirement gradProgramRule : gradProgramRules) {
            requiredCredits = Integer.parseInt(gradProgramRule.getProgramRequirementCode().getRequiredCredits().trim());
            totalCredits = 0;
            if (gradProgramRule.getProgramRequirementCode().getRequiredLevel().trim().compareTo("") != 0) {
                String requiredLevel = gradProgramRule.getProgramRequirementCode().getRequiredLevel().trim();
                totalCredits = studentCourses
                        .stream()
                        .filter(sc -> sc.getCourseLevel().contains(requiredLevel) && !sc.getCourseCode().startsWith("X"))
                        .mapToInt(StudentCourse::getCredits)
                        .sum();
            }
            setCoursesReqMet(studentCourses,gradProgramRule,requiredCredits);

            if (totalCredits >= requiredCredits) {
                logger.debug("{} Passed",gradProgramRule.getProgramRequirementCode().getLabel());
                gradProgramRule.getProgramRequirementCode().setPassed(true);

                List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMet();

                if (reqsMet == null)
                    reqsMet = new ArrayList<>();

                reqsMet.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getTraxReqNumber(),
                        gradProgramRule.getProgramRequirementCode().getLabel()));
                ruleProcessorData.setRequirementsMet(reqsMet);
            } else {
                logger.debug("{} Failed!",gradProgramRule.getProgramRequirementCode().getDescription());
                ruleProcessorData.setGraduated(false);

                List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasons();

                if (nonGradReasons == null)
                    nonGradReasons = new ArrayList<>();

                nonGradReasons.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getTraxReqNumber(),
                        gradProgramRule.getProgramRequirementCode().getNotMetDesc()));
                ruleProcessorData.setNonGradReasons(nonGradReasons);
            }

            logger.info("Min Credits -> Required: {} Has : {}",requiredCredits,totalCredits);
        }

        studentCourses.addAll(tempStudentCourseList.stream().filter(StudentCourse::isUsedInMatchRule).collect(Collectors.toList()));
        ruleProcessorData.setStudentCourses(studentCourses);
        return ruleProcessorData;
    }
    
    private void setCoursesReqMet(List<StudentCourse> studentCourses, ProgramRequirement gradProgramRule, int requiredCredits) {
    	//setting those course who have met this rule
        int tC=0;
        for(StudentCourse sc:studentCourses) {
            if(sc.getCourseLevel().contains(gradProgramRule.getProgramRequirementCode().getRequiredLevel().trim()) && !sc.getCourseCode().startsWith("X")) {
        		tC += sc.getCredits();
                processReqMet(sc,gradProgramRule);
                if (tC > requiredCredits) {
                    break;
                }

            }
        }		
	}

	public void processReqMet(StudentCourse sc, ProgramRequirement gradProgramRule) {
		sc.setUsed(true);
		if (sc.getGradReqMet().length() > 0) {

            sc.setGradReqMet(sc.getGradReqMet() + ", " + gradProgramRule.getProgramRequirementCode().getTraxReqNumber());
            sc.setGradReqMetDetail(sc.getGradReqMetDetail() + ", " + gradProgramRule.getProgramRequirementCode().getTraxReqNumber()
                    + " - " + gradProgramRule.getProgramRequirementCode().getLabel());
        } else {
            sc.setGradReqMet(gradProgramRule.getProgramRequirementCode().getTraxReqNumber());
            sc.setGradReqMetDetail(gradProgramRule.getProgramRequirementCode().getTraxReqNumber() + " - " + gradProgramRule.getProgramRequirementCode().getLabel());
        }
    }
    
    

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("MinCredit1986Rule: Rule Processor Data set.");
    }
}
