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
public class MinElectiveCredits1996Rule implements Rule {

	private static Logger logger = LoggerFactory.getLogger(MinElectiveCredits1996Rule.class);

	@Autowired
	private RuleProcessorData ruleProcessorData;

	public RuleData fire() {
		int totalCredits = 0;
		int requiredCredits;
		logger.debug("Min Elective Credits Rule");

		if (ruleProcessorData.getStudentCourses() == null || ruleProcessorData.getStudentCourses().isEmpty()) {
			logger.warn("!!!Empty list sent to Min Elective Credits Rule for processing");
			return ruleProcessorData;
		}
		Map<String,Integer> map1996 = ruleProcessorData.getMap1996Crse();
		int ldCourseCounter = 0;
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
					if (map1996.get(sc.getCourseCode()) != null) {
						if (sc.getCourseCode().startsWith("X")) {
							if (ldCourseCounter < 8) {
								ldCourseCounter += map1996.get(sc.getCourseCode());
							} else {
								continue;
							}
						}
						if (totalCredits + map1996.get(sc.getCourseCode()) <= requiredCredits) {
							totalCredits += map1996.get(sc.getCourseCode());
							sc.setCreditsUsedForGrad(map1996.get(sc.getCourseCode()));
						} else {
							int extraCredits = totalCredits + map1996.get(sc.getCourseCode()) - requiredCredits;
							totalCredits = requiredCredits;
							sc.setCreditsUsedForGrad(map1996.get(sc.getCourseCode()) - extraCredits);
						}
						if (sc.getGradReqMet().length() > 0) {

							sc.setGradReqMet(sc.getGradReqMet() + ", " + gradProgramRule.getProgramRequirementCode().getTraxReqNumber());
							sc.setGradReqMetDetail(sc.getGradReqMetDetail() + ", " + gradProgramRule.getProgramRequirementCode().getTraxReqNumber() + " - "
									+ gradProgramRule.getProgramRequirementCode().getLabel());
						} else {
							sc.setGradReqMet(gradProgramRule.getProgramRequirementCode().getTraxReqNumber());
							sc.setGradReqMetDetail(
									gradProgramRule.getProgramRequirementCode().getTraxReqNumber() + " - " + gradProgramRule.getProgramRequirementCode().getLabel());
						}
						sc.setUsed(true);
					} else {
						if (!sc.isUsedInMatchRule()) {
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

								sc.setGradReqMet(sc.getGradReqMet() + ", " + gradProgramRule.getProgramRequirementCode().getTraxReqNumber());
								sc.setGradReqMetDetail(sc.getGradReqMetDetail() + ", " + gradProgramRule.getProgramRequirementCode().getTraxReqNumber() + " - "
										+ gradProgramRule.getProgramRequirementCode().getLabel());
							} else {
								sc.setGradReqMet(gradProgramRule.getProgramRequirementCode().getTraxReqNumber());
								sc.setGradReqMetDetail(
										gradProgramRule.getProgramRequirementCode().getTraxReqNumber() + " - " + gradProgramRule.getProgramRequirementCode().getLabel());
							}
							sc.setUsed(true);
						}
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
		logger.info("MinElectiveCredits1996Rule: Rule Processor Data set.");
	}

}
