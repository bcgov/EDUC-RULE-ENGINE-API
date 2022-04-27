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
		logger.debug("Min Elective Credits Rule");

		if (ruleProcessorData.getStudentCourses() == null || ruleProcessorData.getStudentCourses().isEmpty()) {
			logger.warn("!!!Empty list sent to Min Elective Credits Rule for processing");
			return ruleProcessorData;
		}

		List<StudentCourse> studentCourses = RuleProcessorRuleUtils
				.getUniqueStudentCourses(ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());

		logger.debug("Unique Courses: {}",studentCourses.size());

		List<ProgramRequirement> gradProgramRules = ruleProcessorData
				.getGradProgramRules().stream().filter(gpr -> "MCE".compareTo(gpr.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
						&& "Y".compareTo(gpr.getProgramRequirementCode().getActiveRequirement()) == 0 && "C".compareTo(gpr.getProgramRequirementCode().getRequirementCategory()) == 0)
				.collect(Collectors.toList());

		for (ProgramRequirement gradProgramRule : gradProgramRules) {
			requiredCredits = Integer.parseInt(gradProgramRule.getProgramRequirementCode().getRequiredCredits().trim()); // list
			for (StudentCourse sc : studentCourses) {
				if(sc.isUsedInMatchRule() && sc.getLeftOverCredits() != null && sc.getLeftOverCredits() != 0) {
					if (totalCredits + sc.getLeftOverCredits() <= requiredCredits) {
						totalCredits += sc.getLeftOverCredits();
						sc.setCreditsUsedForGrad(sc.getCreditsUsedForGrad() + sc.getLeftOverCredits());
					} else {
						int extraCredits = totalCredits + sc.getLeftOverCredits() - requiredCredits;
						totalCredits = requiredCredits;
						sc.setCreditsUsedForGrad(sc.getCreditsUsedForGrad() + sc.getLeftOverCredits() - extraCredits);
					}
					setGradReqMet(sc,gradProgramRule);
				}

				if(!sc.isUsedInMatchRule()){
					if (totalCredits + sc.getCredits() <= requiredCredits) {
						totalCredits += sc.getCredits();
						sc.setCreditsUsedForGrad(sc.getCredits());
					} else {
						int extraCredits = totalCredits + sc.getCredits() - requiredCredits;
						totalCredits = requiredCredits;
						sc.setCreditsUsedForGrad(sc.getCredits() - extraCredits);
					}
					setGradReqMet(sc,gradProgramRule);
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
				logger.debug("Min Elective Credits Rule: Total-{} Required : {}",totalCredits,requiredCredits);

			} else {
				logger.debug("{} Failed!",gradProgramRule.getProgramRequirementCode().getDescription());
				ruleProcessorData.setGraduated(false);

				List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasons();

				if (nonGradReasons == null)
					nonGradReasons = new ArrayList<>();

				nonGradReasons.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getTraxReqNumber(), gradProgramRule.getProgramRequirementCode().getNotMetDesc()));
				ruleProcessorData.setNonGradReasons(nonGradReasons);
			}

			logger.debug("Min Elective Credits -> Required:{} Has: {}",requiredCredits,totalCredits);
			totalCredits = 0;
		}
		ruleProcessorData.getStudentCourses().addAll(ruleProcessorData.getExcludedCourses());
		return ruleProcessorData;
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
		sc.setUsed(true);

	}

	@Override
	public void setInputData(RuleData inputData) {
		ruleProcessorData = (RuleProcessorData) inputData;
		logger.info("MinElectiveCreditsRule: Rule Processor Data set.");
	}

}
