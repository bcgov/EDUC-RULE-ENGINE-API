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
import ca.bc.gov.educ.api.ruleengine.dto.GradRequirement;
import ca.bc.gov.educ.api.ruleengine.dto.OptionalProgramRequirement;
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
public class DualDogwoodMatchCreditsRule implements Rule {

	private static Logger logger = LoggerFactory.getLogger(DualDogwoodMatchCreditsRule.class);

	@Autowired
	private RuleProcessorData ruleProcessorData;

	public RuleData fire() {

		if (!ruleProcessorData.isHasSpecialProgramDualDogwood()) {
			return ruleProcessorData;
		}
		ruleProcessorData.setSpecialProgramDualDogwoodGraduated(true);
		List<GradRequirement> requirementsMet = new ArrayList<>();
		List<GradRequirement> requirementsNotMet = new ArrayList<>();

		List<StudentCourse> courseList = RuleProcessorRuleUtils.getUniqueStudentCourses(
				ruleProcessorData.getStudentCoursesForDualDogwood(), ruleProcessorData.isProjected());
		List<OptionalProgramRequirement> gradSpecialProgramRulesMatch = ruleProcessorData
				.getGradSpecialProgramRulesDualDogwood().stream()
				.filter(gradSpecialProgramRule -> "M".compareTo(gradSpecialProgramRule.getOptionalProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
						&& "Y".compareTo(gradSpecialProgramRule.getOptionalProgramRequirementCode().getActiveRequirement()) == 0
						&& "C".compareTo(gradSpecialProgramRule.getOptionalProgramRequirementCode().getRequirementCategory()) == 0)
				.collect(Collectors.toList());
		List<CourseRequirement> courseRequirements = ruleProcessorData.getCourseRequirements();

		logger.debug("#### Match Special Program Rule size: " + gradSpecialProgramRulesMatch.size());

		ListIterator<StudentCourse> courseIterator = courseList.listIterator();

		List<StudentCourse> finalCourseList = new ArrayList<>();
		List<OptionalProgramRequirement> finalSpecialProgramRulesList = new ArrayList<>();
		StudentCourse tempSC;
		OptionalProgramRequirement tempSPR;
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

			OptionalProgramRequirement tempSpecialProgramRule = null;

			if (!tempCourseRequirement.isEmpty()) {
                for(CourseRequirement cr:tempCourseRequirement) {
                	if(tempSpecialProgramRule == null) {
						tempSpecialProgramRule = gradSpecialProgramRulesMatch.stream()
								.filter(pr -> pr.getOptionalProgramRequirementCode().getOptProReqCode().compareTo(cr.getRuleCode().getCourseRequirementCode()) == 0).findAny()
								.orElse(null);
                	}
                }
			}
			logger.debug("Temp Program Rule: " + tempSpecialProgramRule);

			if (!tempCourseRequirement.isEmpty() && tempSpecialProgramRule != null) {

				OptionalProgramRequirement finalTempProgramRule = tempSpecialProgramRule;
				if (requirementsMet.stream().filter(rm -> rm.getRule().equals(finalTempProgramRule.getOptionalProgramRequirementCode().getOptProReqCode())).findAny()
						.orElse(null) == null) {
					tempCourse.setUsed(true);
					tempCourse.setCreditsUsedForGrad(tempCourse.getCredits());

					if (tempCourse.getGradReqMet().length() > 0) {

						tempCourse.setGradReqMet(
								tempCourse.getGradReqMet() + ", " + tempSpecialProgramRule.getOptionalProgramRequirementCode().getOptProReqCode());
						tempCourse.setGradReqMetDetail(
								tempCourse.getGradReqMetDetail() + ", " + tempSpecialProgramRule.getOptionalProgramRequirementCode().getOptProReqCode() + " - "
										+ tempSpecialProgramRule.getOptionalProgramRequirementCode().getLabel());
					} else {
						tempCourse.setGradReqMet(tempSpecialProgramRule.getOptionalProgramRequirementCode().getOptProReqCode());
						tempCourse.setGradReqMetDetail(tempSpecialProgramRule.getOptionalProgramRequirementCode().getOptProReqCode() + " - "
								+ tempSpecialProgramRule.getOptionalProgramRequirementCode().getLabel());
					}

					tempSpecialProgramRule.getOptionalProgramRequirementCode().setPassed(true);
					requirementsMet.add(new GradRequirement(tempSpecialProgramRule.getOptionalProgramRequirementCode().getOptProReqCode(),
							tempSpecialProgramRule.getOptionalProgramRequirementCode().getLabel()));
				} else {
					logger.debug("!!! Program Rule met Already: " + tempSpecialProgramRule);
				}
			}

			tempSC = new StudentCourse();
			tempSPR = new OptionalProgramRequirement();
			try {
				tempSC = objectMapper.readValue(objectMapper.writeValueAsString(tempCourse), StudentCourse.class);
				if (tempSC != null)
					finalCourseList.add(tempSC);
				logger.debug("TempSC: " + tempSC);
				logger.debug("Final course List size: : " + finalCourseList.size());
				tempSPR = objectMapper.readValue(objectMapper.writeValueAsString(tempSpecialProgramRule),
						OptionalProgramRequirement.class);
				if (tempSPR != null)
					finalSpecialProgramRulesList.add(tempSPR);
				logger.debug("TempPR: " + tempSPR);
				logger.debug("Final Program rules list size: " + finalSpecialProgramRulesList.size());
			} catch (IOException e) {
				logger.error("ERROR:" + e.getMessage());
			}
		}

		ruleProcessorData.setStudentCoursesForDualDogwood(finalCourseList);

		List<OptionalProgramRequirement> unusedRules = null;
		if(gradSpecialProgramRulesMatch.size() != finalSpecialProgramRulesList.size()) {
    		unusedRules = RuleEngineApiUtils.getCloneSpecialProgramRule(gradSpecialProgramRulesMatch);
    		unusedRules.removeAll(finalSpecialProgramRulesList);
    		finalSpecialProgramRulesList.addAll(unusedRules);
    	}
		
		List<OptionalProgramRequirement> failedRules = finalSpecialProgramRulesList.stream().filter(pr -> !pr.getOptionalProgramRequirementCode().isPassed())
				.collect(Collectors.toList());

		if (failedRules.isEmpty()) {
			logger.debug("All the match rules met!");
		} else {
			for (OptionalProgramRequirement failedRule : failedRules) {
				requirementsNotMet.add(new GradRequirement(failedRule.getOptionalProgramRequirementCode().getOptProReqCode(), failedRule.getOptionalProgramRequirementCode().getNotMetDesc()));
			}
			ruleProcessorData.setSpecialProgramDualDogwoodGraduated(false);

			List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasonsSpecialProgramsDualDogwood();

			if (nonGradReasons == null)
				nonGradReasons = new ArrayList<>();

			nonGradReasons.addAll(requirementsNotMet);
			ruleProcessorData.setNonGradReasonsSpecialProgramsDualDogwood(nonGradReasons);
			logger.debug("One or more Match rules not met!");
		}

		List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMetSpecialProgramsDualDogwood();

		if (reqsMet == null)
			reqsMet = new ArrayList<>();

		reqsMet.addAll(requirementsMet);

		ruleProcessorData.setRequirementsMetSpecialProgramsDualDogwood(reqsMet);
		return ruleProcessorData;
	}

	@Override
	public void setInputData(RuleData inputData) {
		ruleProcessorData = (RuleProcessorData) inputData;
		logger.info("DualDogwoodMatchCreditsRule: Rule Processor Data set.");
	}

}
