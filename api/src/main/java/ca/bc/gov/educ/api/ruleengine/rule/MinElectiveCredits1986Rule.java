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
		logger.debug("Min Elective Credits Rule");
		Integer ldCourseCounter = ruleProcessorData.getLdCounter();
		if (ruleProcessorData.getStudentCourses() == null || ruleProcessorData.getStudentCourses().isEmpty()) {
			logger.warn("!!!Empty list sent to Min Elective Credits Rule for processing");
			return ruleProcessorData;
		}
		List<StudentCourse> studentCourses = RuleProcessorRuleUtils
				.getUniqueStudentCourses(ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());
		Collections.sort(studentCourses, Comparator.comparing(StudentCourse::getCourseLevel).reversed()
				.thenComparing(StudentCourse::getCompletedCoursePercentage).reversed());
		logger.debug("Unique Courses: {}",studentCourses.size());

		List<ProgramRequirement> gradProgramRules = ruleProcessorData
				.getGradProgramRules().stream().filter(gpr -> "MCE".compareTo(gpr.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
						&& "Y".compareTo(gpr.getProgramRequirementCode().getActiveRequirement()) == 0 && "C".compareTo(gpr.getProgramRequirementCode().getRequirementCategory()) == 0)
				.collect(Collectors.toList());

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
						if (sc.getGradReqMet().length() > 0) {

							sc.setGradReqMet(sc.getGradReqMet() + ", " + gradProgramRule.getProgramRequirementCode().getProReqCode());
							sc.setGradReqMetDetail(sc.getGradReqMetDetail() + ", " + gradProgramRule.getProgramRequirementCode().getProReqCode() + " - "
									+ gradProgramRule.getProgramRequirementCode().getLabel());
						} else {
							sc.setGradReqMet(gradProgramRule.getProgramRequirementCode().getProReqCode());
							sc.setGradReqMetDetail(
									gradProgramRule.getProgramRequirementCode().getProReqCode() + " - " + gradProgramRule.getProgramRequirementCode().getLabel());
						}
						sc.setUsed(true);
					}

					if (totalCredits == requiredCredits) {
						break;
					}
				}
				if (totalCredits >= requiredCredits) {
					logger.debug("{} Passed",gradProgramRule.getProgramRequirementCode().getLabel());
					gradProgramRule.getProgramRequirementCode().setPassed(true);

					List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMet();

					if (reqsMet == null)
						reqsMet = new ArrayList<>();

					reqsMet.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getProReqCode(), gradProgramRule.getProgramRequirementCode().getLabel()));
					ruleProcessorData.setRequirementsMet(reqsMet);
					logger.debug("Min Elective Credits Rule: Total-{} Required- {}",totalCredits,requiredCredits);

				} else {
					logger.debug("{} Failed!",gradProgramRule.getProgramRequirementCode().getDescription());
					ruleProcessorData.setGraduated(false);

					List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasons();

					if (nonGradReasons == null)
						nonGradReasons = new ArrayList<>();

					nonGradReasons.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getProReqCode(), gradProgramRule.getProgramRequirementCode().getNotMetDesc()));
					ruleProcessorData.setNonGradReasons(nonGradReasons);
				}

				logger.info("Min Elective Credits -> Required: {} Has : {}",requiredCredits,totalCredits);
				totalCredits = 0;
			}
		}

		ruleProcessorData.getStudentCourses().addAll(ruleProcessorData.getExcludedCourses());
		return ruleProcessorData;
	}



	@Override
	public void setInputData(RuleData inputData) {
		ruleProcessorData = (RuleProcessorData) inputData;
		logger.info("MinElectiveCredits1986Rule: Rule Processor Data set.");
	}

}