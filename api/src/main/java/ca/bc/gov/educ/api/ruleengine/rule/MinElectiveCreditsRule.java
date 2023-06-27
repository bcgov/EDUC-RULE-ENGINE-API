package ca.bc.gov.educ.api.ruleengine.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.ruleengine.dto.ProgramRequirement;
import ca.bc.gov.educ.api.ruleengine.dto.GradRequirement;
import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class MinElectiveCreditsRule implements Rule {

	private static Logger logger = LoggerFactory.getLogger(MinElectiveCreditsRule.class);

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
		/*
		Usually for Matching Electives, you would only pick the courses that were not already used in a match credits rule before
		But, for 2023-EN and 2023-PF programs, you could still use the courses that were already used to match req 14 (Indigenous Requirement)
		 */
		if(!sc.isUsedInMatchRule() ||
				 (gradProgramRule.getGraduationProgramCode().contains("2023") && "14".compareTo(sc.getGradReqMet()) == 0)
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
			&& (!gradProgramRule.getGraduationProgramCode().contains("2023") && "14".compareTo(sc.getGradReqMet()) != 0)) {
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


	@Override
	public void setInputData(RuleData inputData) {
		ruleProcessorData = (RuleProcessorData) inputData;
		logger.debug("MinElectiveCreditsRule: Rule Processor Data set.");
	}

}
