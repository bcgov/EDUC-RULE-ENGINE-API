package ca.bc.gov.educ.api.ruleengine.rule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

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
		Map<String,OptionalProgramRuleProcessor> mapOptional = ruleProcessorData.getMapOptional();
		OptionalProgramRuleProcessor obj = mapOptional.get("FI");
		if (obj == null || !obj.isHasOptionalProgram()) {
			return ruleProcessorData;
		}
		obj.setOptionalProgramGraduated(true);
		List<GradRequirement> requirementsMet = new ArrayList<>();
		List<GradRequirement> requirementsNotMet = new ArrayList<>();

		List<StudentCourse> courseList = RuleProcessorRuleUtils.getUniqueStudentCourses(
				obj.getStudentCoursesOptionalProgram(), ruleProcessorData.isProjected());
		List<OptionalProgramRequirement> gradOptionalProgramRulesMatch = obj
				.getOptionalProgramRules().stream()
				.filter(gradOptionalProgramRule -> "M".compareTo(gradOptionalProgramRule.getOptionalProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
						&& "Y".compareTo(gradOptionalProgramRule.getOptionalProgramRequirementCode().getActiveRequirement()) == 0
						&& "C".compareTo(gradOptionalProgramRule.getOptionalProgramRequirementCode().getRequirementCategory()) == 0)
				.collect(Collectors.toList());
		List<CourseRequirement> courseRequirements = ruleProcessorData.getCourseRequirements();

		logger.debug("#### Match Optional Program Rule size: " + gradOptionalProgramRulesMatch.size());

		ListIterator<StudentCourse> courseIterator = courseList.listIterator();

		List<StudentCourse> finalCourseList = new ArrayList<>();
		List<OptionalProgramRequirement> finalOptionalProgramRulesList = new ArrayList<>();
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

			OptionalProgramRequirement tempOptionalProgramRule = null;

			if (!tempCourseRequirement.isEmpty()) {
                for(CourseRequirement cr:tempCourseRequirement) {
                	if(tempOptionalProgramRule == null) {
						tempOptionalProgramRule = gradOptionalProgramRulesMatch.stream()
								.filter(pr -> pr.getOptionalProgramRequirementCode().getOptProReqCode().compareTo(cr.getRuleCode().getCourseRequirementCode()) == 0).findAny()
								.orElse(null);
                	}
                }
			}
			logger.debug("Temp Program Rule: " + tempOptionalProgramRule);

			if (!tempCourseRequirement.isEmpty() && tempOptionalProgramRule != null) {

				OptionalProgramRequirement finalTempProgramRule = tempOptionalProgramRule;
				if (requirementsMet.stream().filter(rm -> rm.getRule().equals(finalTempProgramRule.getOptionalProgramRequirementCode().getOptProReqCode())).findAny()
						.orElse(null) == null) {
					tempCourse.setUsed(true);
					tempCourse.setCreditsUsedForGrad(tempCourse.getCredits());

					if (tempCourse.getGradReqMet().length() > 0) {

						tempCourse.setGradReqMet(
								tempCourse.getGradReqMet() + ", " + tempOptionalProgramRule.getOptionalProgramRequirementCode().getOptProReqCode());
						tempCourse.setGradReqMetDetail(
								tempCourse.getGradReqMetDetail() + ", " + tempOptionalProgramRule.getOptionalProgramRequirementCode().getOptProReqCode() + " - "
										+ tempOptionalProgramRule.getOptionalProgramRequirementCode().getLabel());
					} else {
						tempCourse.setGradReqMet(tempOptionalProgramRule.getOptionalProgramRequirementCode().getOptProReqCode());
						tempCourse.setGradReqMetDetail(tempOptionalProgramRule.getOptionalProgramRequirementCode().getOptProReqCode() + " - "
								+ tempOptionalProgramRule.getOptionalProgramRequirementCode().getLabel());
					}

					tempOptionalProgramRule.getOptionalProgramRequirementCode().setPassed(true);
					requirementsMet.add(new GradRequirement(tempOptionalProgramRule.getOptionalProgramRequirementCode().getOptProReqCode(),
							tempOptionalProgramRule.getOptionalProgramRequirementCode().getLabel()));
				} else {
					logger.debug("!!! Program Rule met Already: " + tempOptionalProgramRule);
				}
			}
			try {
				tempSC = objectMapper.readValue(objectMapper.writeValueAsString(tempCourse), StudentCourse.class);
				if (tempSC != null)
					finalCourseList.add(tempSC);
				logger.debug("TempSC: " + tempSC);
				logger.debug("Final course List size: : " + finalCourseList.size());
				tempSPR = objectMapper.readValue(objectMapper.writeValueAsString(tempOptionalProgramRule),
						OptionalProgramRequirement.class);
				if (tempSPR != null)
					finalOptionalProgramRulesList.add(tempSPR);
				logger.debug("TempPR: " + tempSPR);
				logger.debug("Final Program rules list size: " + finalOptionalProgramRulesList.size());
			} catch (IOException e) {
				logger.error("ERROR:" + e.getMessage());
			}
		}

		obj.setStudentCoursesOptionalProgram(finalCourseList);
		if(gradOptionalProgramRulesMatch.size() != finalOptionalProgramRulesList.size()) {
			List<OptionalProgramRequirement> unusedRules = RuleEngineApiUtils.getCloneOptionalProgramRule(gradOptionalProgramRulesMatch);
    		unusedRules.removeAll(finalOptionalProgramRulesList);
    		finalOptionalProgramRulesList.addAll(unusedRules);
    	}
		List<OptionalProgramRequirement> failedRules = finalOptionalProgramRulesList.stream().filter(pr -> !pr.getOptionalProgramRequirementCode().isPassed())
				.collect(Collectors.toList());

		if (failedRules.isEmpty()) {
			logger.debug("All the match rules met!");
		} else {
			for (OptionalProgramRequirement failedRule : failedRules) {
				requirementsNotMet.add(new GradRequirement(failedRule.getOptionalProgramRequirementCode().getOptProReqCode(), failedRule.getOptionalProgramRequirementCode().getNotMetDesc()));
			}
			obj.setOptionalProgramGraduated(false);

			List<GradRequirement> nonGradReasons = obj.getNonGradReasonsOptionalProgram();

			if (nonGradReasons == null)
				nonGradReasons = new ArrayList<>();

			nonGradReasons.addAll(requirementsNotMet);
			obj.setNonGradReasonsOptionalProgram(nonGradReasons);
			logger.debug("One or more Match rules not met!");
		}

		List<GradRequirement> resMet = obj.getRequirementsMetOptionalProgram();

		if (resMet == null)
			resMet = new ArrayList<>();

		resMet.addAll(requirementsMet);

		obj.setRequirementsMetOptionalProgram(resMet);
		mapOptional.put("FI",obj);
		ruleProcessorData.setMapOptional(mapOptional);
		return ruleProcessorData;
	}

	@Override
	public void setInputData(RuleData inputData) {
		ruleProcessorData = (RuleProcessorData) inputData;
		logger.info("FrenchImmersionMatchRule: Rule Processor Data set.");
	}

}
