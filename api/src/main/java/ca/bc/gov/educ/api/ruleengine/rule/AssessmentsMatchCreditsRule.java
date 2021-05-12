package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.struct.*;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentsMatchCreditsRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(AssessmentsMatchCreditsRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    final RuleType ruleType = RuleType.MATCH;

    public RuleData fire() {

        List<GradRequirement> requirementsMet = new ArrayList<GradRequirement>();
        List<GradRequirement> requirementsNotMet = new ArrayList<GradRequirement>();
        List<StudentAssessment> assessmentList = RuleProcessorRuleUtils.getUniqueStudentAssessments(
                ruleProcessorData.getStudentAssessments(), ruleProcessorData.isProjected());
        List<StudentAssessment> excludedAssessments = RuleProcessorRuleUtils.getExcludedStudentAssessments(
                ruleProcessorData.getStudentAssessments(), ruleProcessorData.isProjected());
        ruleProcessorData.setExcludedAssessments(excludedAssessments);
        logger.debug("Unique Assessments: " + assessmentList.size());

        List<GradProgramRule> gradProgramRulesMatch = ruleProcessorData.getGradProgramRules()
                .stream()
                .filter(gradProgramRule -> "M".compareTo(gradProgramRule.getRequirementType()) == 0
                        && "Y".compareTo(gradProgramRule.getIsActive()) == 0
                        && "A".compareTo(gradProgramRule.getRuleCategory()) == 0)
                .collect(Collectors.toList());

        List<AssessmentRequirement> assessmentRequirements = ruleProcessorData.getAssessmentRequirements();
        List<AssessmentRequirement> originalAssessmentRequirements = new ArrayList<AssessmentRequirement>(assessmentRequirements);

        logger.debug("#### Match Program Rule size: " + gradProgramRulesMatch.size());

        List<StudentAssessment> finalAssessmentList = new ArrayList<StudentAssessment>();
        List<GradProgramRule> finalProgramRulesList = new ArrayList<GradProgramRule>();
        StudentAssessment tempSA;
        GradProgramRule tempPR;
        ObjectMapper objectMapper = new ObjectMapper();

        ListIterator<StudentAssessment> assessmentIterator = assessmentList.listIterator();

        while (assessmentIterator.hasNext()) {
        	StudentAssessment tempAssessment = assessmentIterator.next();

            logger.debug("Processing Assessment: Code=" + tempAssessment.getAssessmentCode());
            logger.debug("Assessment Requirements size: " + assessmentRequirements.size());

            AssessmentRequirement tempAssessmentRequirement = assessmentRequirements.stream()
                    .filter(ar -> tempAssessment.getAssessmentCode().compareTo(ar.getAssessmentCode()) == 0)
                    .findAny()
                    .orElse(null);

            logger.debug("Temp Assessment Requirement: " + tempAssessmentRequirement);

            GradProgramRule tempProgramRule = null;

            if (tempAssessmentRequirement != null) {
                tempProgramRule = gradProgramRulesMatch.stream()
                        .filter(pr -> pr.getRuleCode().compareTo(tempAssessmentRequirement.getRuleCode()) == 0)
                        .findAny()
                        .orElse(null);
            }
            logger.debug("Temp Program Rule: " + tempProgramRule);

            if (tempAssessmentRequirement != null && tempProgramRule != null) {

                GradProgramRule finalTempProgramRule = tempProgramRule;
                if (requirementsMet.stream()
                        .filter(rm -> rm.getRule() == finalTempProgramRule.getRuleCode())
                        .findAny().orElse(null) == null) {
                    tempAssessment.setUsed(true);

                    if (tempAssessment.getGradReqMet().length() > 0) {

                    	tempAssessment.setGradReqMet(tempAssessment.getGradReqMet() + ", " + tempProgramRule.getRuleCode());
                    	tempAssessment.setGradReqMetDetail(tempAssessment.getGradReqMetDetail() + ", " + tempProgramRule.getRuleCode()
                                + " - " + tempProgramRule.getRequirementName());
                    } else {
                    	tempAssessment.setGradReqMet(tempProgramRule.getRuleCode());
                    	tempAssessment.setGradReqMetDetail(tempProgramRule.getRuleCode() + " - " + tempProgramRule.getRequirementName());
                    }

                    tempProgramRule.setPassed(true);
                    requirementsMet.add(new GradRequirement(tempProgramRule.getRuleCode(), tempProgramRule.getRequirementName()));
                } else {
                    logger.debug("!!! Program Rule met Already: " + tempProgramRule);
                }
            }

            tempSA = new StudentAssessment();
            tempPR = new GradProgramRule();
            try {
            	tempSA = objectMapper.readValue(objectMapper.writeValueAsString(tempAssessment), StudentAssessment.class);
                if (tempSA != null)
                    finalAssessmentList.add(tempSA);
                logger.debug("TempSC: " + tempSA);
                logger.debug("Final Assessment List size: : " + finalAssessmentList.size());
                tempPR = objectMapper.readValue(objectMapper.writeValueAsString(tempProgramRule), GradProgramRule.class);
                if (tempPR != null && !finalProgramRulesList.contains(tempPR)) {
                    finalProgramRulesList.add(tempPR);
                }
                logger.debug("TempPR: " + tempPR);
                logger.debug("Final Program rules list size: " + finalProgramRulesList.size());
            } catch (IOException e) {
                logger.error("ERROR:" + e.getMessage());
            }
        }

        logger.debug("Final Program rules list: " + finalProgramRulesList);

        List<GradProgramRule> failedRules = finalProgramRulesList.stream()
                .filter(pr -> !pr.isPassed()).collect(Collectors.toList());

        if (failedRules.isEmpty()) {
            logger.debug("All the match rules met!");
        } else {
            for (GradProgramRule failedRule : failedRules) {
                requirementsNotMet.add(new GradRequirement(failedRule.getRuleCode(), failedRule.getNotMetDesc()));
            }

            logger.info("One or more Match rules not met!");
            ruleProcessorData.setGraduated(false);

            List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasons();

            if (nonGradReasons == null)
                nonGradReasons = new ArrayList<GradRequirement>();

            nonGradReasons.addAll(requirementsNotMet);
            ruleProcessorData.setNonGradReasons(nonGradReasons);
        }

        //finalProgramRulesList only has the Match type rules in it. Add rest of the type of rules back to the list.
        finalProgramRulesList.addAll(ruleProcessorData.getGradProgramRules()
                .stream()
                .filter(gradProgramRule -> "M".compareTo(gradProgramRule.getRequirementType()) != 0)
                .collect(Collectors.toList()));

        logger.debug("Final Program rules list size 2: " + finalProgramRulesList.size());
        finalAssessmentList.addAll(ruleProcessorData.getExcludedAssessments());
        ruleProcessorData.setStudentAssessments(finalAssessmentList);
        ruleProcessorData.setGradProgramRules(finalProgramRulesList);
        ruleProcessorData.setAssessmentRequirements(originalAssessmentRequirements);

        List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMet();

        if (reqsMet == null)
            reqsMet = new ArrayList<GradRequirement>();

        reqsMet.addAll(requirementsMet);
        ruleProcessorData.setRequirementsMet(reqsMet);

        return ruleProcessorData;
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("AssessmentsMatchCreditsRule: Rule Processor Data set.");
    }

}
