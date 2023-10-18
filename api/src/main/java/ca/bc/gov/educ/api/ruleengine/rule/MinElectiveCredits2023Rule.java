package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class MinElectiveCredits2023Rule implements Rule {

	private static Logger logger = LoggerFactory.getLogger(MinElectiveCredits2023Rule.class);

	@Autowired
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
				.toList();

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
		/*
		Usually for Matching Electives, you would only pick the courses that were not already used in a match credits rule before
		But, for 2023-EN and 2023-PF programs, you could still use the courses that were already used to match req 14 (Indigenous Requirement)
		 */
		if(!sc.isUsedInMatchRule() ||
				 (gradProgramRule.getGraduationProgramCode().contains("2023") && !hasMatchTypeRule(sc.getGradReqMet()))
		){
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
		if((sc.isUsedInMatchRule() && sc.getLeftOverCredits() != null && sc.getLeftOverCredits() != 0)
			&& (!gradProgramRule.getGraduationProgramCode().contains("2023") && !hasMatchTypeRule(sc.getGradReqMet()))) {
				if (totalCredits + sc.getLeftOverCredits() <= requiredCredits) {
					totalCredits += sc.getLeftOverCredits();
					sc.setCreditsUsedForGrad(sc.getCreditsUsedForGrad() + sc.getLeftOverCredits());
				} else {
					int extraCredits = totalCredits + sc.getLeftOverCredits() - requiredCredits;
					totalCredits = requiredCredits;
					sc.setCreditsUsedForGrad(sc.getCreditsUsedForGrad() + sc.getLeftOverCredits() - extraCredits);
				}
				AlgorithmSupportRule.setGradReqMet(sc, gradProgramRule);
		}
		return totalCredits;
	}

	private boolean hasMatchTypeRule(String gradReqMet) {
		if (!RuleProcessorUtils.isEmptyOrNull(gradReqMet)) {
			String[] gradReqMetList = gradReqMet.split(",");

			//Trim items in the list
			for (int i = 0; i < gradReqMetList.length; i++)
				gradReqMetList[i] = gradReqMetList[i].trim();

			List<ProgramRequirement> matchProgramRequirements = ruleProcessorData.getGradProgramRules().stream()
					.filter(pr -> "M".compareTo(pr.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0)
					.toList();

			for (ProgramRequirement pr : matchProgramRequirements) {
				if (Arrays.asList(gradReqMetList).contains(pr.getProgramRequirementCode().getTraxReqNumber()))
					return true;
			}
		}
		return false;
	}

	@Override
	public void setInputData(RuleData inputData) {
		ruleProcessorData = (RuleProcessorData) inputData;
	}
}
