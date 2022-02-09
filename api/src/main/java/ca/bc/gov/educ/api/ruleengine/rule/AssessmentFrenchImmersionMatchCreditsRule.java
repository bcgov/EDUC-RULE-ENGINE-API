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
public class AssessmentFrenchImmersionMatchCreditsRule implements Rule {

	private static Logger logger = LoggerFactory.getLogger(AssessmentFrenchImmersionMatchCreditsRule.class);

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

		List<StudentAssessment> assessmentList = RuleProcessorRuleUtils.getUniqueStudentAssessments(
				obj.getStudentAssessmentsOptionalProgram(), ruleProcessorData.isProjected());
		List<OptionalProgramRequirement> gradOptionalProgramRulesMatch = obj
				.getOptionalProgramRules().stream()
				.filter(gradOptionalProgramRule -> "M".compareTo(gradOptionalProgramRule.getOptionalProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
						&& "Y".compareTo(gradOptionalProgramRule.getOptionalProgramRequirementCode().getActiveRequirement()) == 0
						&& "A".compareTo(gradOptionalProgramRule.getOptionalProgramRequirementCode().getRequirementCategory()) == 0)
				.collect(Collectors.toList());
		List<AssessmentRequirement> assessmentRequirements = ruleProcessorData.getAssessmentRequirements();
		if(assessmentRequirements == null) {
			assessmentRequirements = new ArrayList<>();
		}
		logger.debug("#### Match Optional Program Rule size: " + gradOptionalProgramRulesMatch.size());

		ListIterator<StudentAssessment> assessmentIterator = assessmentList.listIterator();

		List<StudentAssessment> finalAssessmentList = new ArrayList<>();
		List<OptionalProgramRequirement> finalOptionalProgramRulesList = new ArrayList<>();
		StudentAssessment tempSC;
		OptionalProgramRequirement tempSPR;
		ObjectMapper objectMapper = new ObjectMapper();

		while (assessmentIterator.hasNext()) {
			StudentAssessment tempAssessment = assessmentIterator.next();

            logger.debug("Processing Assessment: Code=" + tempAssessment.getAssessmentCode());
            logger.debug("Assessment Requirements size: " + assessmentRequirements.size());

            List<AssessmentRequirement> tempAssessmentRequirement = assessmentRequirements.stream()
                    .filter(ar -> tempAssessment.getAssessmentCode().compareTo(ar.getAssessmentCode()) == 0)
                    .collect(Collectors.toList());

            logger.debug("Temp Assessment Requirement: " + tempAssessmentRequirement);

            OptionalProgramRequirement tempOptionalProgramRule = null;
            if (!tempAssessmentRequirement.isEmpty()) {
                for(AssessmentRequirement ar:tempAssessmentRequirement) {
                	if(tempOptionalProgramRule == null) {
                		tempOptionalProgramRule = gradOptionalProgramRulesMatch.stream()
                        .filter(pr -> pr.getOptionalProgramRequirementCode().getOptProReqCode().compareTo(ar.getRuleCode().getAssmtRequirementCode()) == 0)
                        .findAny()
                        .orElse(null);
                	}
                }
            }
			
			logger.debug("Temp Program Rule: " + tempOptionalProgramRule);

			if (!tempAssessmentRequirement.isEmpty() && tempOptionalProgramRule != null) {

				OptionalProgramRequirement finalTempProgramRule = tempOptionalProgramRule;
				if (requirementsMet.stream().filter(rm -> rm.getRule().equals(finalTempProgramRule.getOptionalProgramRequirementCode().getOptProReqCode())).findAny()
						.orElse(null) == null) {
					tempAssessment.setUsed(true);

					if (tempAssessment.getGradReqMet().length() > 0) {

						tempAssessment.setGradReqMet(
								tempAssessment.getGradReqMet() + ", " + tempOptionalProgramRule.getOptionalProgramRequirementCode().getOptProReqCode());
						tempAssessment.setGradReqMetDetail(
								tempAssessment.getGradReqMetDetail() + ", " + tempOptionalProgramRule.getOptionalProgramRequirementCode().getOptProReqCode() + " - "
										+ tempOptionalProgramRule.getOptionalProgramRequirementCode().getLabel());
					} else {
						tempAssessment.setGradReqMet(tempOptionalProgramRule.getOptionalProgramRequirementCode().getOptProReqCode());
						tempAssessment.setGradReqMetDetail(tempOptionalProgramRule.getOptionalProgramRequirementCode().getOptProReqCode() + " - "
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
				tempSC = objectMapper.readValue(objectMapper.writeValueAsString(tempAssessment), StudentAssessment.class);
				if (tempSC != null)
					finalAssessmentList.add(tempSC);
				logger.debug("TempSC: " + tempSC);
				logger.debug("Final Assessment List size: : " + finalAssessmentList.size());
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

		obj.setStudentAssessmentsOptionalProgram(finalAssessmentList);
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
		logger.info("AssessmentFrenchImmersionMatchCreditsRule: Rule Processor Data set.");
	}

}
