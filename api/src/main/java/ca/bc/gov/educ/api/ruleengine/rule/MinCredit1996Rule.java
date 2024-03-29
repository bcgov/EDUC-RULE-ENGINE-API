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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class MinCredit1996Rule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(MinCredit1996Rule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    public RuleData fire() {
        int totalCredits;
        int requiredCredits;

        List<StudentCourse> tempStudentCourseList = RuleProcessorRuleUtils.getUniqueStudentCourses(
                ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());
        List<StudentCourse> studentCourses = tempStudentCourseList.stream().filter(sc -> !sc.isUsedInMatchRule() || (sc.getLeftOverCredits() != null && sc.getLeftOverCredits() > 0)).collect(Collectors.toList());
        studentCourses.sort(Comparator.comparing(StudentCourse::getCourseLevel, Comparator.reverseOrder())
                .thenComparing(StudentCourse::getCompletedCoursePercentage, Comparator.reverseOrder()));
        List<ProgramRequirement> gradProgramRules = ruleProcessorData.getGradProgramRules()
                .stream()
                .filter(gpr -> "MC".compareTo(gpr.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
                            && "Y".compareTo(gpr.getProgramRequirementCode().getActiveRequirement()) == 0
                            && "C".compareTo(gpr.getProgramRequirementCode().getRequirementCategory()) == 0)
                .distinct().toList();

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

            List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMet();
            List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasons();
            GradRequirement gr = new GradRequirement(gradProgramRule.getProgramRequirementCode().getTraxReqNumber(),
                    gradProgramRule.getProgramRequirementCode().getLabel(),gradProgramRule.getProgramRequirementCode().getProReqCode());

            if (totalCredits >= requiredCredits) {
                logger.debug("{} Passed",gradProgramRule.getProgramRequirementCode().getLabel());
                gradProgramRule.getProgramRequirementCode().setPassed(true);

                if (reqsMet == null)
                    reqsMet = new ArrayList<>();
                if (nonGradReasons == null)
                    nonGradReasons = new ArrayList<>();

                if (!reqsMet.contains(gr)) {
                    reqsMet.add(gr);
                    ruleProcessorData.setRequirementsMet(reqsMet);
                }

                //When you add the requirement to ReqMet List, remove them from the NotGradReasons list if they exist
                nonGradReasons.remove(gr);
                ruleProcessorData.setNonGradReasons(nonGradReasons);
            } else {
                logger.debug("{} Failed!",gradProgramRule.getProgramRequirementCode().getDescription());
                ruleProcessorData.setGraduated(false);

                if (nonGradReasons == null)
                    nonGradReasons = new ArrayList<>();

                if (!nonGradReasons.contains(gr)) {
                    nonGradReasons.add(gr);
                    ruleProcessorData.setNonGradReasons(nonGradReasons);
                }
            }

            logger.debug("Min Credits -> Required: {} Has : {}",requiredCredits,totalCredits);
        }

        studentCourses.addAll(tempStudentCourseList.stream().filter(sc -> sc.isUsedInMatchRule() && sc.getLeftOverCredits() == null).collect(Collectors.toList()));
        ruleProcessorData.setStudentCourses(studentCourses);
        return ruleProcessorData;
    }
    
    private void setCoursesReqMet(List<StudentCourse> studentCourses, ProgramRequirement gradProgramRule, int requiredCredits) {
    	//setting those course who have met this rule
        int tC=0;
        for(StudentCourse sc:studentCourses) {
            if(sc.getCourseLevel().contains(gradProgramRule.getProgramRequirementCode().getRequiredLevel().trim()) && !sc.getCourseCode().startsWith("X")) {
        		tC += sc.getCredits();
                processReqMet(sc,gradProgramRule, tC, requiredCredits);
                if (tC > requiredCredits) {
                    break;
                }
            }
        }		
	}

	public void processReqMet(StudentCourse sc, ProgramRequirement gradProgramRule, int totalCredits, int requiredCredits) {
		sc.setUsed(true);
        sc.setUsedInMinCreditRule(true);
        if (totalCredits > requiredCredits) {
            int leftOverCredit = totalCredits - requiredCredits;
            sc.setCreditsUsedForGrad(sc.getCredits() - leftOverCredit);
            sc.setLeftOverCredits(leftOverCredit);
        }
        AlgorithmSupportRule.setGradReqMet(sc,gradProgramRule);
    }
    
    

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
    }
}
