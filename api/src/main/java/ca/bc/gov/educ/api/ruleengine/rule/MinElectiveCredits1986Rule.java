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

import java.util.*;
import java.util.stream.Collectors;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class MinElectiveCredits1986Rule implements Rule {

	private static Logger logger = LoggerFactory.getLogger(MinElectiveCredits1986Rule.class);

	@Autowired
	private RuleProcessorData ruleProcessorData;

	public RuleData fire() {
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
						totalCredits = processExtraCredits(extraCreditsUsed,extraCreditsLDcrses,sc,totalCredits,requiredCredits);
						setGradReqMet(sc,gradProgramRule);
						sc.setUsed(true);
					}

					if (totalCredits == requiredCredits) {
						break;
					}
				}
				checkCredits(totalCredits,requiredCredits,gradProgramRule);
				totalCredits = 0;
			}
		}

		ruleProcessorData.getStudentCourses().addAll(ruleProcessorData.getExcludedCourses());
		return ruleProcessorData;
	}

	private void checkCredits(int totalCredits, int requiredCredits, ProgramRequirement gradProgramRule) {
		if (totalCredits >= requiredCredits) {
			logger.debug("{} Passed",gradProgramRule.getProgramRequirementCode().getLabel());
			gradProgramRule.getProgramRequirementCode().setPassed(true);

			List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMet();

			if (reqsMet == null)
				reqsMet = new ArrayList<>();

			reqsMet.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getTraxReqNumber(), gradProgramRule.getProgramRequirementCode().getLabel()));
			ruleProcessorData.setRequirementsMet(reqsMet);
			logger.debug("Min Elective Credits Rule: Total-{} Required- {}",totalCredits,requiredCredits);

		} else {
			logger.debug("{} Failed!",gradProgramRule.getProgramRequirementCode().getDescription());
			ruleProcessorData.setGraduated(false);

			List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasons();

			if (nonGradReasons == null)
				nonGradReasons = new ArrayList<>();

			nonGradReasons.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getTraxReqNumber(), gradProgramRule.getProgramRequirementCode().getNotMetDesc()));
			ruleProcessorData.setNonGradReasons(nonGradReasons);
		}
	}
	private void setGradReqMet(StudentCourse sc, ProgramRequirement gradProgramRule) {
		if (sc.getGradReqMet().length() > 0) {

			sc.setGradReqMet(sc.getGradReqMet() + ", " + gradProgramRule.getProgramRequirementCode().getTraxReqNumber());
			sc.setGradReqMetDetail(sc.getGradReqMetDetail() + ", " + gradProgramRule.getProgramRequirementCode().getTraxReqNumber() + " - "
					+ gradProgramRule.getProgramRequirementCode().getLabel());
		} else {
			sc.setGradReqMet(gradProgramRule.getProgramRequirementCode().getTraxReqNumber());
			sc.setGradReqMetDetail(
					gradProgramRule.getProgramRequirementCode().getTraxReqNumber() + " - " + gradProgramRule.getProgramRequirementCode().getLabel());
		}
	}

	private int processExtraCredits(boolean extraCreditsUsed, int extraCreditsLDcrses, StudentCourse sc, int totalCredits, int requiredCredits) {
		if (extraCreditsUsed && extraCreditsLDcrses != 0) {
			if (totalCredits + extraCreditsLDcrses <= requiredCredits) {
				totalCredits += extraCreditsLDcrses;
				sc.setCreditsUsedForGrad(extraCreditsLDcrses);
			} else {
				int extraCredits = totalCredits + extraCreditsLDcrses - requiredCredits;
				totalCredits = requiredCredits;
				sc.setCreditsUsedForGrad(extraCreditsLDcrses - extraCredits);
			}
		} else {
			if (totalCredits + sc.getCredits() <= requiredCredits) {
				totalCredits += sc.getCredits();
				sc.setCreditsUsedForGrad(sc.getCredits());
			} else {
				int extraCredits = totalCredits + sc.getCredits() - requiredCredits;
				totalCredits = requiredCredits;
				sc.setCreditsUsedForGrad(sc.getCredits() - extraCredits);
			}
		}
		return totalCredits;
	}



	@Override
	public void setInputData(RuleData inputData) {
		ruleProcessorData = (RuleProcessorData) inputData;
		logger.info("MinElectiveCredits1986Rule: Rule Processor Data set.");
	}

}
