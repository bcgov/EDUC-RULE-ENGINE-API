package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class MinCreditsElective12OtherRule implements Rule {

	private static Logger logger = LoggerFactory.getLogger(MinCreditsElective12OtherRule.class);

	private RuleProcessorData ruleProcessorData;

	public RuleData fire() {
		int totalCredits = 0;
		int requiredCredits;

		List<GradRequirement> requirementsNotMet = new ArrayList<>();

		List<StudentCourse> studentCourses = RuleProcessorRuleUtils
				.getUniqueStudentCourses(ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());

		List<ProgramRequirement> gradProgramRules = ruleProcessorData
				.getGradProgramRules().stream().filter(gpr -> "MCEOTHER".compareTo(gpr.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
						&& "Y".compareTo(gpr.getProgramRequirementCode().getActiveRequirement()) == 0 && "C".compareTo(gpr.getProgramRequirementCode().getRequirementCategory()) == 0)
				.collect(Collectors.toList());

		if (studentCourses == null || studentCourses.isEmpty()) {
			logger.warn("!!!Empty list sent to Min Elective Credits Rule for processing");
			AlgorithmSupportRule.processEmptyCourseCondition(ruleProcessorData,ruleProcessorData.getGradProgramRules(),requirementsNotMet);
			return ruleProcessorData;
		}

		for (ProgramRequirement gradProgramRule : gradProgramRules) {
			requiredCredits = Integer.parseInt(gradProgramRule.getProgramRequirementCode().getRequiredCredits().trim()); // list

			List<StudentCourse> tempStudentCourseList;

			if (gradProgramRule.getProgramRequirementCode().getRequiredLevel() == null
					|| gradProgramRule.getProgramRequirementCode().getRequiredLevel().trim().compareTo("") == 0) {
				tempStudentCourseList = studentCourses.stream().filter(sc -> !sc.isUsed()).collect(Collectors.toList());
			} else {
				tempStudentCourseList = studentCourses.stream()
						.filter(sc -> !sc.isUsed()
								&& sc.getCourseLevel().compareTo(gradProgramRule.getProgramRequirementCode().getRequiredLevel().trim()) == 0)
						.collect(Collectors.toList());
			}
			int courseFound = 0;
			for (StudentCourse sc : tempStudentCourseList) {
				if(sc.getCourseLevel().contains("12") && !sc.isNotEligibleForElective()) {
					courseFound++;
					if (totalCredits + sc.getCredits() <= requiredCredits) {
						totalCredits += sc.getCredits();
						sc.setCreditsUsedForGrad(sc.getCredits());
					} else {
						int extraCredits = totalCredits + sc.getCredits() - requiredCredits;
						totalCredits = requiredCredits;
						sc.setCreditsUsedForGrad(sc.getCredits() - extraCredits);
					}
					AlgorithmSupportRule.setGradReqMet(sc,gradProgramRule);
					sc.setUsed(true);
				}

				if (totalCredits == requiredCredits) {
					break;
				}
			}
			if(courseFound == 0) {
				boolean reqCourseFound = false;
				for(StudentCourse sc:studentCourses) {
					if(sc.getGradReqMet().compareTo("3") == 0) {
						reqCourseFound = true;
						AlgorithmSupportRule.setGradReqMet(sc,gradProgramRule);
					}
				}
				if(reqCourseFound) {
					gradProgramRule.getProgramRequirementCode().setPassed(true);
					List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMet();
					
					if (reqsMet == null)
						reqsMet = new ArrayList<>();
	
					reqsMet.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getTraxReqNumber(), gradProgramRule.getProgramRequirementCode().getLabel(),gradProgramRule.getProgramRequirementCode().getProReqCode()));
					ruleProcessorData.setRequirementsMet(reqsMet);
					List<GradRequirement> delReqsMet = ruleProcessorData.getRequirementsMet();
					if(delReqsMet != null)
						delReqsMet.removeIf(e -> e.getRule() != null && e.getRule().compareTo("502") == 0);
				}else {
					ruleProcessorData.setGraduated(false);
					
					List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasons();
	
					if (nonGradReasons == null)
						nonGradReasons = new ArrayList<>();
	
					nonGradReasons.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getTraxReqNumber(), gradProgramRule.getProgramRequirementCode().getNotMetDesc(),gradProgramRule.getProgramRequirementCode().getProReqCode()));
					ruleProcessorData.setNonGradReasons(nonGradReasons);
				}
				
			}else {
				if (totalCredits >= requiredCredits) {
					logger.debug("{} Passed",gradProgramRule.getProgramRequirementCode().getLabel());
					gradProgramRule.getProgramRequirementCode().setPassed(true);
					List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMet();
	
					if (reqsMet == null)
						reqsMet = new ArrayList<>();
	
					reqsMet.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getTraxReqNumber(), gradProgramRule.getProgramRequirementCode().getLabel(),gradProgramRule.getProgramRequirementCode().getProReqCode()));
					ruleProcessorData.setRequirementsMet(reqsMet);
					logger.debug("Min Credits Elective 12 Rule: Total- {} Required- {}",totalCredits,requiredCredits);
	
				} else {
					logger.debug("{} Failed!",gradProgramRule.getProgramRequirementCode().getDescription());
					ruleProcessorData.setGraduated(false);
	
					List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasons();
	
					if (nonGradReasons == null)
						nonGradReasons = new ArrayList<>();
	
					nonGradReasons.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getTraxReqNumber(), gradProgramRule.getProgramRequirementCode().getNotMetDesc(),gradProgramRule.getProgramRequirementCode().getProReqCode()));
					ruleProcessorData.setNonGradReasons(nonGradReasons);
				}
				studentCourses.stream()
                .filter(sc -> sc.getGradReqMet().compareTo("3") == 0)
                .forEach(sc -> {                	
						sc.setUsed(false);
						sc.setGradReqMet("");
						sc.setGradReqMetDetail("");
					
                });
				List<GradRequirement> delReqsMet = ruleProcessorData.getRequirementsMet();
				delReqsMet.removeIf(e -> e.getRule() != null && e.getRule().compareTo("502") == 0);
				if(ruleProcessorData.getNonGradReasons() != null) {
					List<GradRequirement> delNonGradReason = ruleProcessorData.getNonGradReasons();
					delNonGradReason.removeIf(e -> e.getRule() != null && e.getRule().compareTo("502") == 0);
				}
			}

			logger.info("Min Elective Credits 12 Other -> Required: {} Has: {}",requiredCredits,totalCredits);

		}
		ruleProcessorData.setStudentCourses(studentCourses);
		return ruleProcessorData;
	}

}
