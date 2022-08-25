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
public class MinElectiveCreditsRule implements Rule {

	private static Logger logger = LoggerFactory.getLogger(MinElectiveCreditsRule.class);

	private RuleProcessorData ruleProcessorData;

	public RuleData fire() {
		int totalCredits = 0;
		int requiredCredits;
		List<GradRequirement> requirementsNotMet = new ArrayList<>();

		List<StudentCourse> studentCourses = RuleProcessorRuleUtils
				.getUniqueStudentCourses(ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());

		List<ProgramRequirement> gradProgramRules = ruleProcessorData
				.getGradProgramRules().stream().filter(gpr -> "MCE".compareTo(gpr.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
						&& "Y".compareTo(gpr.getProgramRequirementCode().getActiveRequirement()) == 0 && "C".compareTo(gpr.getProgramRequirementCode().getRequirementCategory()) == 0)
				.collect(Collectors.toList());

		if (studentCourses == null || studentCourses.isEmpty()) {
			logger.warn("!!!Empty list sent to Min Elective Credits Rule for processing");
			AlgorithmSupportRule.processEmptyCourseCondition(ruleProcessorData,ruleProcessorData.getGradProgramRules(),requirementsNotMet);
			return ruleProcessorData;
		}

		for (ProgramRequirement gradProgramRule : gradProgramRules) {
			requiredCredits = Integer.parseInt(gradProgramRule.getProgramRequirementCode().getRequiredCredits().trim()); // list
			for (StudentCourse sc : studentCourses) {
				totalCredits = processLeftOverCredits(sc,requiredCredits,totalCredits,gradProgramRule);
				totalCredits = processOthers(sc,requiredCredits,totalCredits,gradProgramRule);
				if (totalCredits == requiredCredits) {
					break;
				}

			}
			AlgorithmSupportRule.checkCredits(totalCredits,requiredCredits,gradProgramRule,ruleProcessorData);
			totalCredits = 0;
		}
		ruleProcessorData.getStudentCourses().addAll(ruleProcessorData.getExcludedCourses());
		return ruleProcessorData;
	}

	private int processOthers(StudentCourse sc, int requiredCredits, int totalCredits, ProgramRequirement gradProgramRule) {
		if(!sc.isUsedInMatchRule()){
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
		return totalCredits;
	}
	private int processLeftOverCredits(StudentCourse sc, int requiredCredits, int totalCredits, ProgramRequirement gradProgramRule) {
		if(sc.isUsedInMatchRule() && sc.getLeftOverCredits() != null && sc.getLeftOverCredits() != 0) {
			if (totalCredits + sc.getLeftOverCredits() <= requiredCredits) {
				totalCredits += sc.getLeftOverCredits();
				sc.setCreditsUsedForGrad(sc.getCreditsUsedForGrad() + sc.getLeftOverCredits());
			} else {
				int extraCredits = totalCredits + sc.getLeftOverCredits() - requiredCredits;
				totalCredits = requiredCredits;
				sc.setCreditsUsedForGrad(sc.getCreditsUsedForGrad() + sc.getLeftOverCredits() - extraCredits);
			}
			AlgorithmSupportRule.setGradReqMet(sc,gradProgramRule);
		}
		return totalCredits;
	}

}
