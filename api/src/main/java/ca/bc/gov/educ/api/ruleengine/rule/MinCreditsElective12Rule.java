package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class MinCreditsElective12Rule implements Rule {

	private static Logger logger = LoggerFactory.getLogger(MinCreditsElective12Rule.class);

	@Override
	public RuleData fire(RuleProcessorData ruleProcessorData) {
		int totalCredits = 0;
		int requiredCredits;

		List<StudentCourse> studentCourses = RuleProcessorRuleUtils
				.getUniqueStudentCourses(ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());

		List<ProgramRequirement> gradProgramRules = ruleProcessorData
				.getGradProgramRules().stream().filter(gpr -> "MCE12".compareTo(gpr.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
						&& "Y".compareTo(gpr.getProgramRequirementCode().getActiveRequirement()) == 0 && "C".compareTo(gpr.getProgramRequirementCode().getRequirementCategory()) == 0)
				.collect(Collectors.toList());

		if (studentCourses == null || studentCourses.isEmpty()) {
			logger.warn("!!!Empty list sent to Min Elective Credits Rule for processing");
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
								&& sc.getCourseLevel().contains(gradProgramRule.getProgramRequirementCode().getRequiredLevel().trim()))
						.collect(Collectors.toList());
			}

			for (StudentCourse sc : tempStudentCourseList) {
				if(!sc.isNotEligibleForElective()) {
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
	
					if (totalCredits == requiredCredits) {
						break;
					}
				}
			}

			if (totalCredits >= requiredCredits) {
				logger.debug("{} Passed",gradProgramRule.getProgramRequirementCode().getLabel());
				gradProgramRule.getProgramRequirementCode().setPassed(true);

				List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMet();

				if (reqsMet == null)
					reqsMet = new ArrayList<>();

				reqsMet.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getTraxReqNumber(), gradProgramRule.getProgramRequirementCode().getLabel(),gradProgramRule.getProgramRequirementCode().getProReqCode()));
				ruleProcessorData.setRequirementsMet(reqsMet);
				logger.debug("Min Credits Elective 12 Rule: Total-{} Required- {}",totalCredits,requiredCredits);

			} else {
				logger.debug("{} Failed!",gradProgramRule.getProgramRequirementCode().getDescription());
				ruleProcessorData.setGraduated(false);

				List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasons();

				if (nonGradReasons == null)
					nonGradReasons = new ArrayList<>();

				nonGradReasons.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getTraxReqNumber(), gradProgramRule.getProgramRequirementCode().getNotMetDesc(),gradProgramRule.getProgramRequirementCode().getProReqCode()));
				ruleProcessorData.setNonGradReasons(nonGradReasons);
			}
			//Remove the 502 non grad reason if any as it is not a mandatory requirement
			List<GradRequirement> delNonGradReason = ruleProcessorData.getNonGradReasons();
			if(delNonGradReason != null)
				delNonGradReason.removeIf(e -> e.getRule()!= null && e.getRule().compareTo("502") == 0);
			logger.info("Min Elective Credits -> Required:{} Has: {}",requiredCredits,totalCredits);
			totalCredits = 0;
		}
		ruleProcessorData.setStudentCourses(studentCourses);
		return ruleProcessorData;
	}

}
