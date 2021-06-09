package ca.bc.gov.educ.api.ruleengine.rule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.educ.api.ruleengine.dto.CourseRequirement;
import ca.bc.gov.educ.api.ruleengine.dto.GradProgramRule;
import ca.bc.gov.educ.api.ruleengine.dto.GradRequirement;
import ca.bc.gov.educ.api.ruleengine.dto.GradSpecialProgramRule;
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
public class FrenchImmersionMatchRule implements Rule {

	private static Logger logger = LoggerFactory.getLogger(FrenchImmersionMatchRule.class);

	@Autowired
	private RuleProcessorData ruleProcessorData;

	public RuleData fire() {

		if (!ruleProcessorData.isHasSpecialProgramFrenchImmersion()) {
			return ruleProcessorData;
		}
		ruleProcessorData.setSpecialProgramFrenchImmersionGraduated(true);
		List<GradRequirement> requirementsMet = new ArrayList<>();
		List<GradRequirement> requirementsNotMet = new ArrayList<>();

		List<StudentCourse> courseList = RuleProcessorRuleUtils.getUniqueStudentCourses(
				ruleProcessorData.getStudentCoursesForFrenchImmersion(), ruleProcessorData.isProjected());
		List<GradSpecialProgramRule> gradSpecialProgramRulesMatch = ruleProcessorData
				.getGradSpecialProgramRulesFrenchImmersion().stream()
				.filter(gradSpecialProgramRule -> "M".compareTo(gradSpecialProgramRule.getRequirementType()) == 0
						&& "Y".compareTo(gradSpecialProgramRule.getIsActive()) == 0
						&& "C".compareTo(gradSpecialProgramRule.getRuleCategory()) == 0)
				.collect(Collectors.toList());
		List<CourseRequirement> courseRequirements = ruleProcessorData.getCourseRequirements();

		logger.debug("#### Match Special Program Rule size: " + gradSpecialProgramRulesMatch.size());

		ListIterator<StudentCourse> courseIterator = courseList.listIterator();

		List<StudentCourse> finalCourseList = new ArrayList<>();
		List<GradSpecialProgramRule> finalSpecialProgramRulesList = new ArrayList<>();
		StudentCourse tempSC;
		GradSpecialProgramRule tempSPR;
		ObjectMapper objectMapper = new ObjectMapper();

		while (courseIterator.hasNext()) {
			StudentCourse tempCourse = courseIterator.next();

			logger.debug(
					"Processing Course: Code=" + tempCourse.getCourseCode() + " Level=" + tempCourse.getCourseLevel());
			logger.debug("Course Requirements size: " + courseRequirements.size());

			List<CourseRequirement> tempCourseRequirement = courseRequirements.stream()
                    .filter(cr -> tempCourse.getCourseCode().compareTo(cr.getCourseCode()) == 0
                            && tempCourse.getCourseLevel().compareTo(cr.getCourseLevel()) == 0)
                    .collect(Collectors.toList());

			logger.debug("Temp Course Requirement: " + tempCourseRequirement);

			GradSpecialProgramRule tempSpecialProgramRule = null;

			if (!tempCourseRequirement.isEmpty()) {
                for(CourseRequirement cr:tempCourseRequirement) {
                	if(tempSpecialProgramRule == null) {
						tempSpecialProgramRule = gradSpecialProgramRulesMatch.stream()
								.filter(pr -> pr.getRuleCode().compareTo(cr.getRuleCode()) == 0).findAny()
								.orElse(null);
                	}
                }
			}
			logger.debug("Temp Program Rule: " + tempSpecialProgramRule);

			if (!tempCourseRequirement.isEmpty() && tempSpecialProgramRule != null) {

				GradSpecialProgramRule finalTempProgramRule = tempSpecialProgramRule;
				if (requirementsMet.stream().filter(rm -> rm.getRule().equals(finalTempProgramRule.getRuleCode())).findAny()
						.orElse(null) == null) {
					tempCourse.setUsed(true);
					tempCourse.setCreditsUsedForGrad(tempCourse.getCredits());

					if (tempCourse.getGradReqMet().length() > 0) {

						tempCourse.setGradReqMet(
								tempCourse.getGradReqMet() + ", " + tempSpecialProgramRule.getRuleCode());
						tempCourse.setGradReqMetDetail(
								tempCourse.getGradReqMetDetail() + ", " + tempSpecialProgramRule.getRuleCode() + " - "
										+ tempSpecialProgramRule.getRequirementName());
					} else {
						tempCourse.setGradReqMet(tempSpecialProgramRule.getRuleCode());
						tempCourse.setGradReqMetDetail(tempSpecialProgramRule.getRuleCode() + " - "
								+ tempSpecialProgramRule.getRequirementName());
					}

					tempSpecialProgramRule.setPassed(true);
					requirementsMet.add(new GradRequirement(tempSpecialProgramRule.getRuleCode(),
							tempSpecialProgramRule.getRequirementName()));
				} else {
					logger.debug("!!! Program Rule met Already: " + tempSpecialProgramRule);
				}
			}

			tempSC = new StudentCourse();
			tempSPR = new GradSpecialProgramRule();
			try {
				tempSC = objectMapper.readValue(objectMapper.writeValueAsString(tempCourse), StudentCourse.class);
				if (tempSC != null)
					finalCourseList.add(tempSC);
				logger.debug("TempSC: " + tempSC);
				logger.debug("Final course List size: : " + finalCourseList.size());
				tempSPR = objectMapper.readValue(objectMapper.writeValueAsString(tempSpecialProgramRule),
						GradSpecialProgramRule.class);
				if (tempSPR != null)
					finalSpecialProgramRulesList.add(tempSPR);
				logger.debug("TempPR: " + tempSPR);
				logger.debug("Final Program rules list size: " + finalSpecialProgramRulesList.size());
			} catch (IOException e) {
				logger.error("ERROR:" + e.getMessage());
			}
		}

		ruleProcessorData.setStudentCoursesForFrenchImmersion(finalCourseList);
		
		List<GradSpecialProgramRule> unusedRules = null;
		if(gradSpecialProgramRulesMatch.size() != finalSpecialProgramRulesList.size()) {
    		unusedRules = RuleEngineApiUtils.getCloneSpecialProgramRule(gradSpecialProgramRulesMatch);
    		unusedRules.removeAll(finalSpecialProgramRulesList);
    		finalSpecialProgramRulesList.addAll(unusedRules);
    	}
		List<GradSpecialProgramRule> failedRules = finalSpecialProgramRulesList.stream().filter(pr -> !pr.isPassed())
				.collect(Collectors.toList());

		if (failedRules.isEmpty()) {
			logger.debug("All the match rules met!");
		} else {
			for (GradSpecialProgramRule failedRule : failedRules) {
				requirementsNotMet.add(new GradRequirement(failedRule.getRuleCode(), failedRule.getNotMetDesc()));
			}
			ruleProcessorData.setSpecialProgramFrenchImmersionGraduated(false);

			List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasonsSpecialProgramsFrenchImmersion();

			if (nonGradReasons == null)
				nonGradReasons = new ArrayList<>();

			nonGradReasons.addAll(requirementsNotMet);
			ruleProcessorData.setNonGradReasonsSpecialProgramsFrenchImmersion(nonGradReasons);
			logger.debug("One or more Match rules not met!");
		}

		List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMetSpecialProgramsFrenchImmersion();

		if (reqsMet == null)
			reqsMet = new ArrayList<>();

		reqsMet.addAll(requirementsMet);

		ruleProcessorData.setRequirementsMetSpecialProgramsFrenchImmersion(reqsMet);
		return ruleProcessorData;
	}

	@Override
	public void setInputData(RuleData inputData) {
		ruleProcessorData = (RuleProcessorData) inputData;
		logger.info("FrenchImmersionMatchRule: Rule Processor Data set.");
	}

}
