package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class MinElectiveCredits1986Rule implements Rule {

	private static Logger logger = LoggerFactory.getLogger(MinElectiveCredits1986Rule.class);

	@Override
	public RuleData fire(RuleProcessorData ruleProcessorData) {
		int totalCredits = 0;
		int requiredCredits;

		List<GradRequirement> requirementsNotMet = new ArrayList<>();
		Integer ldCourseCounter = ruleProcessorData.getLdCounter();
		List<StudentCourse> studentCourses = RuleProcessorRuleUtils
				.getUniqueStudentCourses(ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());
		studentCourses.sort(Comparator.comparing(StudentCourse::getCourseLevel).reversed()
				.thenComparing(StudentCourse::getCompletedCoursePercentage).reversed());

		List<ProgramRequirement> gradProgramRules = ruleProcessorData
				.getGradProgramRules().stream().filter(gpr -> "MCE".compareTo(gpr.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
						&& "Y".compareTo(gpr.getProgramRequirementCode().getActiveRequirement()) == 0 && "C".compareTo(gpr.getProgramRequirementCode().getRequirementCategory()) == 0)
				.collect(Collectors.toList());

		if (studentCourses.isEmpty()) {
			logger.warn("!!!Empty list sent to Min Elective Credits Rule for processing");
			AlgorithmSupportRule.processEmptyCourseCondition(ruleProcessorData,ruleProcessorData.getGradProgramRules(),requirementsNotMet);
			return ruleProcessorData;
		}

		for (ProgramRequirement gradProgramRule : gradProgramRules) {
			if(gradProgramRule.getProgramRequirementCode().getRequiredLevel() == null) {
				requiredCredits = Integer.parseInt(gradProgramRule.getProgramRequirementCode().getRequiredCredits().trim()); // list

				for (StudentCourse sc : studentCourses) {
					if (!sc.isUsedInMatchRule() && (sc.getCourseLevel().trim().contains("11") || sc.getCourseLevel().trim().contains("12"))) {
						boolean extraCreditsUsed = false;
						int extraCreditsLDcrses = 0;
						if (sc.getCourseCode().startsWith("X")) {
							if (ldCourseCounter < 8) {
								if (ldCourseCounter + sc.getCredits() <= 8) {
									ldCourseCounter += sc.getCredits();
								} else {
									int extraCredits = ldCourseCounter + sc.getCredits() - 8;
									ldCourseCounter += extraCredits;
									extraCreditsLDcrses = extraCredits;
									extraCreditsUsed = true;
								}
							} else {
								continue;
							}
						}
						totalCredits = AlgorithmSupportRule.processExtraCredits(extraCreditsUsed,extraCreditsLDcrses,sc,totalCredits,requiredCredits);
						AlgorithmSupportRule.setGradReqMet(sc,gradProgramRule);
						sc.setUsed(true);
					}

					if (totalCredits == requiredCredits) {
						break;
					}
				}
				AlgorithmSupportRule.checkCredits(totalCredits,requiredCredits,gradProgramRule,ruleProcessorData);
				totalCredits = 0;
			}
		}

		ruleProcessorData.getStudentCourses().addAll(ruleProcessorData.getExcludedCourses());
		return ruleProcessorData;
	}

}
