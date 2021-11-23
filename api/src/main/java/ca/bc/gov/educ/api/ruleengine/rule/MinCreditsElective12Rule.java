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
public class MinCreditsElective12Rule implements Rule {

	private static Logger logger = LoggerFactory.getLogger(MinCreditsElective12Rule.class);

	@Autowired
	private RuleProcessorData ruleProcessorData;

	public RuleData fire() {
		int totalCredits = 0;
		int requiredCredits;
		logger.debug("Min Credits Elective 12 Rule");

		if (ruleProcessorData.getStudentCourses().isEmpty()) {
			logger.warn("!!!Empty list sent to Min Elective Credits Rule for processing");
			return ruleProcessorData;
		}

		List<StudentCourse> studentCourses = RuleProcessorRuleUtils
				.getUniqueStudentCourses(ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());

		logger.debug("Unique Courses: " + studentCourses.size());

		List<ProgramRequirement> gradProgramRules = ruleProcessorData
				.getGradProgramRules().stream().filter(gpr -> "MCE12".compareTo(gpr.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
						&& "Y".compareTo(gpr.getProgramRequirementCode().getActiveRequirement()) == 0 && "C".compareTo(gpr.getProgramRequirementCode().getRequirementCategory()) == 0)
				.collect(Collectors.toList());

		logger.debug(gradProgramRules.toString());

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
	
					if (totalCredits == requiredCredits) {
						break;
					}
				}
			}

			if (totalCredits >= requiredCredits) {
				logger.info(gradProgramRule.getProgramRequirementCode().getLabel() + " Passed");
				gradProgramRule.getProgramRequirementCode().setPassed(true);

				List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMet();

				if (reqsMet == null)
					reqsMet = new ArrayList<>();

				reqsMet.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getProReqCode(), gradProgramRule.getProgramRequirementCode().getLabel()));
				ruleProcessorData.setRequirementsMet(reqsMet);
				logger.debug("Min Credits Elective 12 Rule: Total-" + totalCredits + " Required-" + requiredCredits);

			} else {
				logger.info(gradProgramRule.getProgramRequirementCode().getDescription() + " Failed!");
				ruleProcessorData.setGraduated(false);

				List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasons();

				if (nonGradReasons == null)
					nonGradReasons = new ArrayList<>();

				nonGradReasons.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getProReqCode(), gradProgramRule.getProgramRequirementCode().getNotMetDesc()));
				ruleProcessorData.setNonGradReasons(nonGradReasons);
			}
			//Remove the 502 non grad reason if any as it is not a mandatory requirement
			List<GradRequirement> delNonGradReason = ruleProcessorData.getNonGradReasons();
			if(delNonGradReason != null)
				delNonGradReason.removeIf(e -> e.getRule().compareTo("502") == 0);
			logger.info("Min Elective Credits -> Required:" + requiredCredits + " Has:" + totalCredits);
			totalCredits = 0;
		}
		ruleProcessorData.setStudentCourses(studentCourses);
		return ruleProcessorData;
	}

	@Override
	public void setInputData(RuleData inputData) {
		ruleProcessorData = (RuleProcessorData) inputData;
		logger.info("MinCreditsElective12Rule: Rule Processor Data set.");
	}

}
