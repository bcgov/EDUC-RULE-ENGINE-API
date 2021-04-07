package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.struct.*;
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
public class MatchCreditsRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(MatchCreditsRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    final RuleType ruleType = RuleType.MATCH;

    public RuleData fire() {

        List<GradRequirement> requirementsMet = new ArrayList<GradRequirement>();
        List<GradRequirement> requirementsNotMet = new ArrayList<GradRequirement>();

        List<StudentCourse> courseList = ruleProcessorData.getStudentCourses();

        List<GradProgramRule> gradProgramRulesMatch = ruleProcessorData.getGradProgramRules()
                .stream()
                .filter(gradProgramRule -> "M".compareTo(gradProgramRule.getRequirementType()) == 0
                        && "Y".compareTo(gradProgramRule.getIsActive()) == 0)
                .collect(Collectors.toList());

        List<CourseRequirement> courseRequirements = ruleProcessorData.getCourseRequirements();
        List<CourseRequirement> originalCourseRequirements = new ArrayList<CourseRequirement>(courseRequirements);

        //logger.debug("Course Requirements: " + courseRequirements);
        logger.debug("#### Match Program Rule size: " + gradProgramRulesMatch.size());

        List<StudentCourse> finalCourseList = new ArrayList<StudentCourse>();
        List<GradProgramRule> finalProgramRulesList = new ArrayList<GradProgramRule>();
        StudentCourse tempSC;
        GradProgramRule tempPR;
        ObjectMapper objectMapper = new ObjectMapper();

        ListIterator<StudentCourse> courseIterator = courseList.listIterator();

        while (courseIterator.hasNext()) {
            StudentCourse tempCourse = courseIterator.next();

            logger.debug("Processing Course: Code=" + tempCourse.getCourseCode() + " Level=" + tempCourse.getCourseLevel());
            logger.debug("Course Requirements size: " + courseRequirements.size());

            CourseRequirement tempCourseRequirement = courseRequirements.stream()
                    .filter(cr -> tempCourse.getCourseCode().compareTo(cr.getCourseCode()) == 0
                            && tempCourse.getCourseLevel().compareTo(cr.getCourseLevel()) == 0)
                    .findAny()
                    .orElse(null);

            logger.debug("Temp Course Requirement: " + tempCourseRequirement);

            GradProgramRule tempProgramRule = null;

            if (tempCourseRequirement != null) {
                tempProgramRule = gradProgramRulesMatch.stream()
                        .filter(pr -> pr.getRuleCode().compareTo(tempCourseRequirement.getRuleCode()) == 0)
                        .findAny()
                        .orElse(null);
            }
            logger.debug("Temp Program Rule: " + tempProgramRule);

            if (tempCourseRequirement != null && tempProgramRule != null) {

                GradProgramRule finalTempProgramRule = tempProgramRule;
                if (requirementsMet.stream()
                        .filter(rm -> rm.getRule() == finalTempProgramRule.getRuleCode())
                        .findAny().orElse(null) == null) {
                    tempCourse.setUsed(true);
                    tempCourse.setCreditsUsedForGrad(tempCourse.getCredits());

                    if (tempCourse.getGradReqMet().length() > 0) {

                        tempCourse.setGradReqMet(tempCourse.getGradReqMet() + ", " + tempProgramRule.getRuleCode());
                        tempCourse.setGradReqMetDetail(tempCourse.getGradReqMetDetail() + ", " + tempProgramRule.getRuleCode()
                                + " - " + tempProgramRule.getRequirementName());
                    } else {
                        tempCourse.setGradReqMet(tempProgramRule.getRuleCode());
                        tempCourse.setGradReqMetDetail(tempProgramRule.getRuleCode() + " - " + tempProgramRule.getRequirementName());
                    }

                    tempProgramRule.setPassed(true);
                    requirementsMet.add(new GradRequirement(tempProgramRule.getRuleCode(), tempProgramRule.getRequirementName()));
                } else {
                    logger.debug("!!! Program Rule met Already: " + tempProgramRule);
                }
            }

            tempSC = new StudentCourse();
            tempPR = new GradProgramRule();
            try {
                tempSC = objectMapper.readValue(objectMapper.writeValueAsString(tempCourse), StudentCourse.class);
                if (tempSC != null)
                    finalCourseList.add(tempSC);
                logger.debug("TempSC: " + tempSC);
                logger.debug("Final course List size: : " + finalCourseList.size());
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

        ruleProcessorData.setStudentCourses(finalCourseList);
        ruleProcessorData.setGradProgramRules(finalProgramRulesList);
        ruleProcessorData.setCourseRequirements(originalCourseRequirements);

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
        logger.info("MatchRule: Rule Processor Data set.");
    }

}
