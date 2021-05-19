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

import ca.bc.gov.educ.api.ruleengine.struct.AssessmentRequirement;
import ca.bc.gov.educ.api.ruleengine.struct.GradRequirement;
import ca.bc.gov.educ.api.ruleengine.struct.GradSpecialProgramRule;
import ca.bc.gov.educ.api.ruleengine.struct.RuleData;
import ca.bc.gov.educ.api.ruleengine.struct.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.struct.StudentAssessment;
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
		List<GradSpecialProgramRule> gradSpecialProgramRulesMatch = ruleProcessorData
				.getGradSpecialProgramRulesDualDogwood().stream()
				.filter(gradSpecialProgramRule -> "M".compareTo(gradSpecialProgramRule.getRequirementType()) == 0
						&& "Y".compareTo(gradSpecialProgramRule.getIsActive()) == 0
						&& "A".compareTo(gradSpecialProgramRule.getRuleCategory()) == 0)
				.collect(Collectors.toList());
		List<AssessmentRequirement> assessmentRequirements = ruleProcessorData.getAssessmentRequirements();

		logger.debug("#### Match Special Program Rule size: " + gradSpecialProgramRulesMatch.size());

		ListIterator<StudentAssessment> assessmentIterator = assessmentList.listIterator();

		List<StudentAssessment> finalAssessmentList = new ArrayList<>();
		List<GradSpecialProgramRule> finalSpecialProgramRulesList = new ArrayList<>();
		StudentAssessment tempSC;
		GradSpecialProgramRule tempSPR;
		ObjectMapper objectMapper = new ObjectMapper();

		while (assessmentIterator.hasNext()) {
			StudentAssessment tempAssessment = assessmentIterator.next();

            logger.debug("Processing Assessment: Code=" + tempAssessment.getAssessmentCode());
            logger.debug("Assessment Requirements size: " + assessmentRequirements.size());

            List<AssessmentRequirement> tempAssessmentRequirement = assessmentRequirements.stream()
                    .filter(ar -> tempAssessment.getAssessmentCode().compareTo(ar.getAssessmentCode()) == 0)
                    .collect(Collectors.toList());

            logger.debug("Temp Assessment Requirement: " + tempAssessmentRequirement);

            GradSpecialProgramRule tempSpecialProgramRule = null;
            if (!tempAssessmentRequirement.isEmpty()) {
                for(AssessmentRequirement ar:tempAssessmentRequirement) {
                	if(tempSpecialProgramRule == null) {
                		tempSpecialProgramRule = gradSpecialProgramRulesMatch.stream()
                        .filter(pr -> pr.getRuleCode().compareTo(ar.getRuleCode()) == 0)
                        .findAny()
                        .orElse(null);
                	}
                }
            }
			
			logger.debug("Temp Program Rule: " + tempSpecialProgramRule);

			if (!tempAssessmentRequirement.isEmpty() && tempSpecialProgramRule != null) {

				GradSpecialProgramRule finalTempProgramRule = tempSpecialProgramRule;
				if (requirementsMet.stream().filter(rm -> rm.getRule().equals(finalTempProgramRule.getRuleCode())).findAny()
						.orElse(null) == null) {
					tempAssessment.setUsed(true);

					if (tempAssessment.getGradReqMet().length() > 0) {

						tempAssessment.setGradReqMet(
								tempAssessment.getGradReqMet() + ", " + tempSpecialProgramRule.getRuleCode());
						tempAssessment.setGradReqMetDetail(
								tempAssessment.getGradReqMetDetail() + ", " + tempSpecialProgramRule.getRuleCode() + " - "
										+ tempSpecialProgramRule.getRequirementName());
					} else {
						tempAssessment.setGradReqMet(tempSpecialProgramRule.getRuleCode());
						tempAssessment.setGradReqMetDetail(tempSpecialProgramRule.getRuleCode() + " - "
								+ tempSpecialProgramRule.getRequirementName());
					}

					tempSpecialProgramRule.setPassed(true);
					requirementsMet.add(new GradRequirement(tempSpecialProgramRule.getRuleCode(),
							tempSpecialProgramRule.getRequirementName()));
				} else {
					logger.debug("!!! Program Rule met Already: " + tempSpecialProgramRule);
				}
			}

			tempSC = new StudentAssessment();
			tempSPR = new GradSpecialProgramRule();
			try {
				tempSC = objectMapper.readValue(objectMapper.writeValueAsString(tempAssessment), StudentAssessment.class);
				if (tempSC != null)
					finalAssessmentList.add(tempSC);
				logger.debug("TempSC: " + tempSC);
				logger.debug("Final Assessment List size: : " + finalAssessmentList.size());
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

		ruleProcessorData.setStudentAssessmentsForDualDogwood(finalAssessmentList);

		List<GradSpecialProgramRule> failedRules = finalSpecialProgramRulesList.stream().filter(pr -> !pr.isPassed())
				.collect(Collectors.toList());

		if (failedRules.isEmpty()) {
			logger.debug("All the match rules met!");
		} else {
			for (GradSpecialProgramRule failedRule : failedRules) {
				requirementsNotMet.add(new GradRequirement(failedRule.getRuleCode(), failedRule.getNotMetDesc()));
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
