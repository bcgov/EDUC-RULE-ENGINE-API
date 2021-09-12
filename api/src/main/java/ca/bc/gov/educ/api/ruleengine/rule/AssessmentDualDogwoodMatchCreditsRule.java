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

import ca.bc.gov.educ.api.ruleengine.dto.AssessmentRequirement;
import ca.bc.gov.educ.api.ruleengine.dto.GradRequirement;
import ca.bc.gov.educ.api.ruleengine.dto.OptionalProgramRequirement;
import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentAssessment;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentDualDogwoodMatchCreditsRule implements Rule {

	private static Logger logger = LoggerFactory.getLogger(AssessmentDualDogwoodMatchCreditsRule.class);

	@Autowired
	private RuleProcessorData ruleProcessorData;

	public RuleData fire() {

		if (!ruleProcessorData.isHasSpecialProgramDualDogwood()) {
			return ruleProcessorData;
		}
		ruleProcessorData.setSpecialProgramDualDogwoodGraduated(true);
		List<GradRequirement> requirementsMet = new ArrayList<>();
		List<GradRequirement> requirementsNotMet = new ArrayList<>();

		List<StudentAssessment> assessmentList = RuleProcessorRuleUtils.getUniqueStudentAssessments(
				ruleProcessorData.getStudentAssessmentsForDualDogwood(), ruleProcessorData.isProjected());
		List<OptionalProgramRequirement> gradSpecialProgramRulesMatch = ruleProcessorData
				.getGradSpecialProgramRulesDualDogwood().stream()
				.filter(gradSpecialProgramRule -> "M".compareTo(gradSpecialProgramRule.getOptionalProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
						&& "Y".compareTo(gradSpecialProgramRule.getOptionalProgramRequirementCode().getActiveRequirement()) == 0
						&& "A".compareTo(gradSpecialProgramRule.getOptionalProgramRequirementCode().getRequirementCategory()) == 0)
				.collect(Collectors.toList());
		List<AssessmentRequirement> assessmentRequirements = ruleProcessorData.getAssessmentRequirements();

		logger.debug("#### Match Special Program Rule size: " + gradSpecialProgramRulesMatch.size());

		ListIterator<StudentAssessment> assessmentIterator = assessmentList.listIterator();

		List<StudentAssessment> finalAssessmentList = new ArrayList<>();
		List<OptionalProgramRequirement> finalSpecialProgramRulesList = new ArrayList<>();
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

            OptionalProgramRequirement tempSpecialProgramRule = null;
            if (!tempAssessmentRequirement.isEmpty()) {
                for(AssessmentRequirement ar:tempAssessmentRequirement) {
                	if(tempSpecialProgramRule == null) {
                		tempSpecialProgramRule = gradSpecialProgramRulesMatch.stream()
                        .filter(pr -> pr.getOptionalProgramRequirementCode().getOptProReqCode().compareTo(ar.getRuleCode().getAssmtRequirementCode()) == 0)
                        .findAny()
                        .orElse(null);
                	}
                }
            }
			
			logger.debug("Temp Program Rule: " + tempSpecialProgramRule);

			if (!tempAssessmentRequirement.isEmpty() && tempSpecialProgramRule != null) {

				OptionalProgramRequirement finalTempProgramRule = tempSpecialProgramRule;
				if (requirementsMet.stream().filter(rm -> rm.getRule().equals(finalTempProgramRule.getOptionalProgramRequirementCode().getOptProReqCode())).findAny()
						.orElse(null) == null) {
					tempAssessment.setUsed(true);

					if (tempAssessment.getGradReqMet().length() > 0) {

						tempAssessment.setGradReqMet(
								tempAssessment.getGradReqMet() + ", " + tempSpecialProgramRule.getOptionalProgramRequirementCode().getOptProReqCode());
						tempAssessment.setGradReqMetDetail(
								tempAssessment.getGradReqMetDetail() + ", " + tempSpecialProgramRule.getOptionalProgramRequirementCode().getOptProReqCode() + " - "
										+ tempSpecialProgramRule.getOptionalProgramRequirementCode().getLabel());
					} else {
						tempAssessment.setGradReqMet(tempSpecialProgramRule.getOptionalProgramRequirementCode().getOptProReqCode());
						tempAssessment.setGradReqMetDetail(tempSpecialProgramRule.getOptionalProgramRequirementCode().getOptProReqCode() + " - "
								+ tempSpecialProgramRule.getOptionalProgramRequirementCode().getLabel());
					}

					tempSpecialProgramRule.getOptionalProgramRequirementCode().setPassed(true);
					requirementsMet.add(new GradRequirement(tempSpecialProgramRule.getOptionalProgramRequirementCode().getOptProReqCode(),
							tempSpecialProgramRule.getOptionalProgramRequirementCode().getLabel()));
				} else {
					logger.debug("!!! Program Rule met Already: " + tempSpecialProgramRule);
				}
			}

			tempSC = new StudentAssessment();
			tempSPR = new OptionalProgramRequirement();
			try {
				tempSC = objectMapper.readValue(objectMapper.writeValueAsString(tempAssessment), StudentAssessment.class);
				if (tempSC != null)
					finalAssessmentList.add(tempSC);
				logger.debug("TempSC: " + tempSC);
				logger.debug("Final Assessment List size: : " + finalAssessmentList.size());
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

		ruleProcessorData.setStudentAssessmentsForDualDogwood(finalAssessmentList);

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
		logger.info("AssessmentDualDogwoodMatchCreditsRule: Rule Processor Data set.");
	}

}
