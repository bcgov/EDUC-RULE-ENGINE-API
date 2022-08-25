package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class MinCredit1996Rule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(MinCredit1996Rule.class);

    @Override
    public RuleData fire(RuleProcessorData ruleProcessorData) {
        int totalCredits;
        int requiredCredits;

        List<StudentCourse> tempStudentCourseList = RuleProcessorRuleUtils.getUniqueStudentCourses(
                ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());
        List<StudentCourse> studentCourses = tempStudentCourseList.stream().filter(sc -> !sc.isUsedInMatchRule()).collect(Collectors.toList());

        List<ProgramRequirement> gradProgramRules = ruleProcessorData.getGradProgramRules()
                .stream()
                .filter(gpr -> "MC".compareTo(gpr.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
                            && "Y".compareTo(gpr.getProgramRequirementCode().getActiveRequirement()) == 0
                            && "C".compareTo(gpr.getProgramRequirementCode().getRequirementCategory()) == 0)
                .collect(Collectors.toList());

        if (tempStudentCourseList.isEmpty()) {
            logger.warn("!!!Empty list sent to Min Credits Rule for processing");
            return ruleProcessorData;
        }

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
                        gradProgramRule.getProgramRequirementCode().getLabel(),gradProgramRule.getProgramRequirementCode().getProReqCode()));
                ruleProcessorData.setRequirementsMet(reqsMet);
            } else {
                logger.debug("{} Failed!",gradProgramRule.getProgramRequirementCode().getDescription());
                ruleProcessorData.setGraduated(false);

                List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasons();

                if (nonGradReasons == null)
                    nonGradReasons = new ArrayList<>();

                nonGradReasons.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getTraxReqNumber(),
                        gradProgramRule.getProgramRequirementCode().getNotMetDesc(),gradProgramRule.getProgramRequirementCode().getProReqCode()));
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
        AlgorithmSupportRule.setGradReqMet(sc,gradProgramRule);
    }

}
