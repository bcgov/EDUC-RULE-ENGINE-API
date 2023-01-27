package ca.bc.gov.educ.api.ruleengine.rule;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
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
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class MinAdultCoursesRule implements Rule {

	private static Logger logger = LoggerFactory.getLogger(MinAdultCoursesRule.class);

	@Autowired
	private RuleProcessorData ruleProcessorData;

	public RuleData fire() {
		logger.debug("Min Adult Courses Rule");

		int totalCredits = 0;
		int requiredCredits;
		
		if (ruleProcessorData.getStudentCourses().isEmpty()) {
			logger.warn("!!!Empty list sent to Min Adult Courses Rule for processing");
			return ruleProcessorData;
		}

		List<StudentCourse> studentCourses = RuleProcessorRuleUtils
				.getUniqueStudentCourses(ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());
		
		String gradDate = RuleProcessorRuleUtils.getGradDate(studentCourses);
		int diff = RuleEngineApiUtils.getDifferenceInMonths(gradDate, "2012-07-01");
		if(diff > 0) {
			return ruleProcessorData;
		}

		List<ProgramRequirement> gradProgramRules = ruleProcessorData
				.getGradProgramRules().stream().filter(gpr -> "MAC".compareTo(gpr.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
						&& "Y".compareTo(gpr.getProgramRequirementCode().getActiveRequirement()) == 0 && "C".compareTo(gpr.getProgramRequirementCode().getRequirementCategory()) == 0)
				.collect(Collectors.toList());

		for (ProgramRequirement gradProgramRule : gradProgramRules) {
			requiredCredits = Integer.parseInt(gradProgramRule.getProgramRequirementCode().getRequiredCredits().trim()); // list

			List<StudentCourse> tempStudentCourseList;

			if (gradProgramRule.getProgramRequirementCode().getRequiredLevel() == null
					|| gradProgramRule.getProgramRequirementCode().getRequiredLevel().trim().compareTo("") == 0) {
				tempStudentCourseList = studentCourses.stream().filter(StudentCourse::isUsed).collect(Collectors.toList());
			} else {
				tempStudentCourseList = studentCourses.stream()
						.filter(sc -> sc.isUsed()
								&& sc.getCourseLevel().compareTo(gradProgramRule.getProgramRequirementCode().getRequiredLevel().trim()) == 0)
						.collect(Collectors.toList());
			}

			for (StudentCourse sc : tempStudentCourseList) {
				String courseSessionDate = sc.getSessionDate() + "/01";
				Date temp = null;
				try {
					temp = RuleEngineApiUtils.parseDate(courseSessionDate, "yyyy/MM/dd");
				} catch (ParseException e) {
					logger.debug(e.getMessage());
				}

				// Get Adult Start date from the Data Object
				Date adultStartDate = ruleProcessorData.getGradStatus().getAdultStartDate();

				if( adultStartDate != null && temp.compareTo(adultStartDate) > 0
						&& (totalCredits + sc.getCredits()) <= requiredCredits) {
					totalCredits += sc.getCredits();
					AlgorithmSupportRule.setGradReqMet(sc,gradProgramRule);
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

				reqsMet.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getTraxReqNumber(), gradProgramRule.getProgramRequirementCode().getLabel(),gradProgramRule.getProgramRequirementCode().getProReqCode()));
				ruleProcessorData.setRequirementsMet(reqsMet);
				logger.debug("Min Adult Courses : Total- {} Required {}",totalCredits,requiredCredits);

			} else {
				logger.info("{} Failed!",gradProgramRule.getProgramRequirementCode().getDescription());
				ruleProcessorData.setGraduated(false);

				List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasons();

				if (nonGradReasons == null)
					nonGradReasons = new ArrayList<>();

				nonGradReasons.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getTraxReqNumber(), gradProgramRule.getProgramRequirementCode().getNotMetDesc(),gradProgramRule.getProgramRequirementCode().getProReqCode()));
				ruleProcessorData.setNonGradReasons(nonGradReasons);
			}
			
			List<GradRequirement> reqMetList = ruleProcessorData.getRequirementsMet().stream().filter(gpr -> gpr.getRule() != null && "4".compareTo(gpr.getRule()) == 0)
			.collect(Collectors.toList());
			if(reqMetList.size() == 2) {
				List<GradRequirement> delNonGradReason = ruleProcessorData.getNonGradReasons();
				if(delNonGradReason != null)
					delNonGradReason.removeIf(e -> e.getRule() != null && e.getRule().compareTo("506") == 0);
			}

			logger.info("Min Adult Courses -> Required: {} Has: {}",requiredCredits,totalCredits);
		}
		ruleProcessorData.getStudentCourses().addAll(ruleProcessorData.getExcludedCourses());
		return ruleProcessorData;
	}

	@Override
	public void setInputData(RuleData inputData) {
		ruleProcessorData = (RuleProcessorData) inputData;
		logger.info("MinAdultCoursesRule: Rule Processor Data set.");
	}

}
