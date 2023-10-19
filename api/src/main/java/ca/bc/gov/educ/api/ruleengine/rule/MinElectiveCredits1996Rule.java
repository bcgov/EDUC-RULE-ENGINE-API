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
import java.util.Map;
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

		List<GradRequirement> requirementsNotMet = new ArrayList<>();

		Map<String,Integer> map1996 = ruleProcessorData.getMap1996Crse();
		int ldCourseCounter = 0;
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
						AlgorithmSupportRule.setGradReqMet(sc,gradProgramRule);
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
							totalCredits = AlgorithmSupportRule.processExtraCredits(extraCreditsUsed,extraCreditsLDcrses,sc,totalCredits,requiredCredits);
							AlgorithmSupportRule.setGradReqMet(sc,gradProgramRule);
							sc.setUsed(true);
						}
					}
					if (totalCredits == requiredCredits) {
						break;
					}
				}
				AlgorithmSupportRule.checkCredits1996(totalCredits,requiredCredits,gradProgramRule,ruleProcessorData);
				totalCredits = 0;
			}
		}

		if(ruleProcessorData.getGradProgram().getProgramCode().equalsIgnoreCase("1996-EN"))
			ruleProcessorData.getStudentCourses().addAll(ruleProcessorData.getExcludedCourses());
		return ruleProcessorData;
	}



	@Override
	public void setInputData(RuleData inputData) {
		ruleProcessorData = (RuleProcessorData) inputData;
	}

}
