package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class AssessmentsMatchCreditsRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(AssessmentsMatchCreditsRule.class);

    @Override
    public RuleData fire(RuleProcessorData ruleProcessorData) {

        List<GradRequirement> requirementsMet = new ArrayList<>();
        List<GradRequirement> requirementsNotMet = new ArrayList<>();
        List<StudentCourse> courseList = RuleProcessorRuleUtils.getUniqueStudentCourses(
                ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());
        List<StudentAssessment> assessmentList = RuleProcessorRuleUtils.getUniqueStudentAssessments(
                ruleProcessorData.getStudentAssessments(), ruleProcessorData.isProjected());

        List<ProgramRequirement> gradProgramRulesMatch = ruleProcessorData.getGradProgramRules()
                .stream()
                .filter(gradProgramRule -> "M".compareTo(gradProgramRule.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
                        && "Y".compareTo(gradProgramRule.getProgramRequirementCode().getActiveRequirement()) == 0
                        && "A".compareTo(gradProgramRule.getProgramRequirementCode().getRequirementCategory()) == 0)
                .collect(Collectors.toList());

        if (ruleProcessorData.getStudentAssessments() == null || ruleProcessorData.getStudentAssessments().isEmpty()) {
            logger.warn("!!!Empty list sent to Assessment Match Rule for processing");
            AlgorithmSupportRule.processEmptyAssessmentCondition(ruleProcessorData,gradProgramRulesMatch,requirementsNotMet);
            return ruleProcessorData;
        }

        List<AssessmentRequirement> assessmentRequirements = ruleProcessorData.getAssessmentRequirements();
        if(assessmentRequirements == null) {
            assessmentRequirements = new ArrayList<>();
        }
        List<AssessmentRequirement> originalAssessmentRequirements = new ArrayList<>(assessmentRequirements);

        List<StudentAssessment> finalAssessmentList = new ArrayList<>();
        List<ProgramRequirement> finalProgramRulesList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        for (StudentAssessment tempAssessment : assessmentList) {

            List<AssessmentRequirement> tempAssessmentRequirement = assessmentRequirements.stream()
                    .filter(ar -> tempAssessment.getAssessmentCode().compareTo(ar.getAssessmentCode()) == 0)
                    .collect(Collectors.toList());

            ProgramRequirement tempProgramRule = null;

            if (!tempAssessmentRequirement.isEmpty()) {
                for (AssessmentRequirement ar : tempAssessmentRequirement) {
                    if (tempProgramRule == null) {
                        tempProgramRule = gradProgramRulesMatch.stream()
                                .filter(pr -> pr.getProgramRequirementCode().getProReqCode().compareTo(ar.getRuleCode().getAssmtRequirementCode()) == 0)
                                .findAny()
                                .orElse(null);
                    }
                }
            }
            logger.debug("Temp Program Rule: {}",tempProgramRule);
            processAssessments(tempAssessmentRequirement,tempProgramRule,requirementsMet,tempAssessment);

            try {
                StudentAssessment tempSA = objectMapper.readValue(objectMapper.writeValueAsString(tempAssessment), StudentAssessment.class);
                if (tempSA != null)
                    finalAssessmentList.add(tempSA);
                logger.debug("TempSC: {}",tempSA);
                logger.debug("Final Assessment List size: : {}",finalAssessmentList.size());
                ProgramRequirement tempPR = objectMapper.readValue(objectMapper.writeValueAsString(tempProgramRule), ProgramRequirement.class);
                if (tempPR != null && !finalProgramRulesList.contains(tempPR)) {
                    finalProgramRulesList.add(tempPR);
                }
                logger.debug("TempPR: {}",tempPR);
                logger.debug("Final Program rules list size: {}",finalProgramRulesList.size());
            } catch (IOException e) {
                logger.error("ERROR: {}",e.getMessage());
            }
        }

        logger.debug("Final Program rules list: {}",finalProgramRulesList);


        processReqMetAndNotMet(ruleProcessorData,finalProgramRulesList,requirementsNotMet,requirementsMet,gradProgramRulesMatch,courseList,finalAssessmentList);

        //finalProgramRulesList only has the Match type rules in it. Add rest of the type of rules back to the list.
        finalProgramRulesList.addAll(ruleProcessorData.getGradProgramRules()
                .stream()
                .filter(gradProgramRule -> "M".compareTo(gradProgramRule.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) != 0)
                .collect(Collectors.toList()));

        logger.debug("Final Program rules list size 2: {}",finalProgramRulesList.size());
        ruleProcessorData.setStudentAssessments(finalAssessmentList);
        ruleProcessorData.setGradProgramRules(finalProgramRulesList);
        ruleProcessorData.setAssessmentRequirements(originalAssessmentRequirements);

        List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMet();

        if (reqsMet == null)
            reqsMet = new ArrayList<>();

        reqsMet.addAll(requirementsMet);
        ruleProcessorData.setRequirementsMet(reqsMet);
        ruleProcessorData.getStudentAssessments().addAll(ruleProcessorData.getExcludedAssessments());
        return ruleProcessorData;
    }

    private void processReqMetAndNotMet(RuleProcessorData ruleProcessorData, List<ProgramRequirement> finalProgramRulesList, List<GradRequirement> requirementsNotMet, List<GradRequirement> requirementsMet, List<ProgramRequirement> gradProgramRulesMatch, List<StudentCourse> courseList, List<StudentAssessment> finalAssessmentList) {
        if(gradProgramRulesMatch.size() != finalProgramRulesList.size()) {
            List<ProgramRequirement> unusedRules = RuleEngineApiUtils.getCloneProgramRule(gradProgramRulesMatch);
            unusedRules.removeAll(finalProgramRulesList);
            finalProgramRulesList.addAll(unusedRules);
        }

        AlgorithmSupportRule.checkCoursesForEquivalency(finalProgramRulesList,courseList,finalAssessmentList,ruleProcessorData,requirementsMet);

        List<ProgramRequirement> failedRules = finalProgramRulesList.stream()
                .filter(pr -> !pr.getProgramRequirementCode().isPassed()).collect(Collectors.toList());

        if (failedRules.isEmpty()) {
            logger.debug("All the match rules met!");
        } else {
            for (ProgramRequirement failedRule : failedRules) {
                requirementsNotMet.add(new GradRequirement(failedRule.getProgramRequirementCode().getTraxReqNumber(), failedRule.getProgramRequirementCode().getNotMetDesc(),failedRule.getProgramRequirementCode().getProReqCode()));
            }

            logger.info("One or more Match rules not met!");
            ruleProcessorData.setGraduated(false);

            List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasons();

            if (nonGradReasons == null)
                nonGradReasons = new ArrayList<>();

            nonGradReasons.addAll(requirementsNotMet);
            ruleProcessorData.setNonGradReasons(nonGradReasons);
        }
    }
    private void processAssessments(List<AssessmentRequirement> tempAssessmentRequirement, ProgramRequirement tempProgramRule, List<GradRequirement> requirementsMet, StudentAssessment tempAssessment) {
        if (!tempAssessmentRequirement.isEmpty() && tempProgramRule != null) {

            if (requirementsMet.stream()
                    .filter(rm -> rm.getRule().equals(tempProgramRule.getProgramRequirementCode().getProReqCode()))
                    .findAny().orElse(null) == null) {
                tempAssessment.setUsed(true);

                if (tempAssessment.getGradReqMet().length() > 0) {

                    tempAssessment.setGradReqMet(tempAssessment.getGradReqMet() + ", " + tempProgramRule.getProgramRequirementCode().getTraxReqNumber());
                    tempAssessment.setGradReqMetDetail(tempAssessment.getGradReqMetDetail() + ", " + tempProgramRule.getProgramRequirementCode().getTraxReqNumber()
                            + " - " + tempProgramRule.getProgramRequirementCode().getLabel());
                } else {
                    tempAssessment.setGradReqMet(tempProgramRule.getProgramRequirementCode().getTraxReqNumber());
                    tempAssessment.setGradReqMetDetail(tempProgramRule.getProgramRequirementCode().getTraxReqNumber() + " - " + tempProgramRule.getProgramRequirementCode().getLabel());
                }

                tempProgramRule.getProgramRequirementCode().setPassed(true);
                requirementsMet.add(new GradRequirement(tempProgramRule.getProgramRequirementCode().getTraxReqNumber(), tempProgramRule.getProgramRequirementCode().getLabel(),tempProgramRule.getProgramRequirementCode().getProReqCode()));
            } else {
                logger.debug("!!! Program Rule met Already: {}",tempProgramRule);
            }
        }
    }
}
