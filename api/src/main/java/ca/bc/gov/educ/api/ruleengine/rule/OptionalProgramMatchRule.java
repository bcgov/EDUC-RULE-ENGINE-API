package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

@Component
public class OptionalProgramMatchRule {
    private static Logger logger = LoggerFactory.getLogger(OptionalProgramMatchRule.class);

    private OptionalProgramMatchRule() {
    }

    public static void processOptionalProgramAssessmentMatchRule(OptionalProgramRuleProcessor obj, RuleProcessorData ruleProcessorData) {
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
        logger.debug("#### Match Optional Program Rule size: {}",gradOptionalProgramRulesMatch.size());

        ListIterator<StudentAssessment> assessmentIterator = assessmentList.listIterator();

        List<StudentAssessment> finalAssessmentList = new ArrayList<>();
        List<OptionalProgramRequirement> finalOptionalProgramRulesList = new ArrayList<>();
        StudentAssessment tempSC;
        OptionalProgramRequirement tempSPR;
        ObjectMapper objectMapper = new ObjectMapper();

        while (assessmentIterator.hasNext()) {
            StudentAssessment tempAssessment = assessmentIterator.next();

            logger.debug("Processing Assessment: Code= {}",tempAssessment.getAssessmentCode());
            logger.debug("Assessment Requirements size: {} ",assessmentRequirements.size());

            List<AssessmentRequirement> tempAssessmentRequirement = assessmentRequirements.stream()
                    .filter(ar -> tempAssessment.getAssessmentCode().compareTo(ar.getAssessmentCode()) == 0)
                    .collect(Collectors.toList());

            logger.debug("Temp Assessment Requirement: {}",tempAssessmentRequirement);
            OptionalProgramRequirement tempOptionalProgramRule = null;
            handleOptionalProgramRule(tempAssessmentRequirement,gradOptionalProgramRulesMatch,requirementsMet,tempAssessment,tempOptionalProgramRule);

            try {
                tempSC = objectMapper.readValue(objectMapper.writeValueAsString(tempAssessment), StudentAssessment.class);
                if (tempSC != null)
                    finalAssessmentList.add(tempSC);
                logger.debug("TempSC: {}",tempSC);
                logger.debug("Final Assessment List size: : {}",finalAssessmentList.size());
                tempSPR = objectMapper.readValue(objectMapper.writeValueAsString(tempOptionalProgramRule),
                        OptionalProgramRequirement.class);
                if (tempSPR != null)
                    finalOptionalProgramRulesList.add(tempSPR);
                logger.debug("TempPR: {}",tempSPR);
                logger.debug("Final Program rules list size: {}",finalOptionalProgramRulesList.size());
            } catch (IOException e) {
                logger.error("ERROR: {}",e.getMessage());
            }
        }

        obj.setStudentAssessmentsOptionalProgram(finalAssessmentList);
        handleRule(gradOptionalProgramRulesMatch,finalOptionalProgramRulesList,requirementsNotMet,obj,requirementsMet);

    }
    public static void handleFailedRules(List<OptionalProgramRequirement> failedRules, List<GradRequirement> requirementsNotMet, OptionalProgramRuleProcessor obj) {
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
    }
    public static void handleOptionalProgramRule(List<AssessmentRequirement> tempAssessmentRequirement, List<OptionalProgramRequirement> gradOptionalProgramRulesMatch, List<GradRequirement> requirementsMet, StudentAssessment tempAssessment, OptionalProgramRequirement tempOptionalProgramRule) {

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

        logger.debug("Temp Program Rule: {}",tempOptionalProgramRule);

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
                logger.debug("!!! Program Rule met Already: {}",tempOptionalProgramRule);
            }
        }
    }

    public static void handleOptionalProgramCourseMatchRule(List<CourseRequirement> tempCourseRequirement, OptionalProgramRequirement tempOptionalProgramRule, List<GradRequirement> requirementsMet, StudentCourse tempCourse, List<OptionalProgramRequirement> gradOptionalProgramRulesMatch) {
        if (!tempCourseRequirement.isEmpty()) {
            for(CourseRequirement cr:tempCourseRequirement) {
                if(tempOptionalProgramRule == null) {
                    tempOptionalProgramRule = gradOptionalProgramRulesMatch.stream()
                            .filter(pr -> pr.getOptionalProgramRequirementCode().getOptProReqCode().compareTo(cr.getRuleCode().getCourseRequirementCode()) == 0).findAny()
                            .orElse(null);
                }
            }
        }
        logger.debug("Temp Program Rule: {}", tempOptionalProgramRule);

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
                logger.debug("!!! Program Rule met Already: {}", tempOptionalProgramRule);
            }
        }
    }
    public static void processOptionalProgramCourseMatchRule(OptionalProgramRuleProcessor obj, RuleProcessorData ruleProcessorData) {
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
        if(courseRequirements == null) {
            courseRequirements = new ArrayList<>();
        }

        logger.debug("#### Match Optional Program Rule size: {}", gradOptionalProgramRulesMatch.size());

        ListIterator<StudentCourse> courseIterator = courseList.listIterator();

        List<StudentCourse> finalCourseList = new ArrayList<>();
        List<OptionalProgramRequirement> finalOptionalProgramRulesList = new ArrayList<>();
        StudentCourse tempSC;
        OptionalProgramRequirement tempSPR;
        ObjectMapper objectMapper = new ObjectMapper();

        while (courseIterator.hasNext()) {
            StudentCourse tempCourse = courseIterator.next();

            logger.debug("Processing Course: Code={} Level={}",tempCourse.getCourseCode(),tempCourse.getCourseLevel());
            logger.debug("Course Requirements size: {}",courseRequirements.size());

            List<CourseRequirement> tempCourseRequirement = courseRequirements.stream()
                    .filter(cr -> tempCourse.getCourseCode().compareTo(cr.getCourseCode()) == 0
                            && tempCourse.getCourseLevel().compareTo(cr.getCourseLevel()) == 0)
                    .collect(Collectors.toList());

            logger.debug("Temp Course Requirement: {}", tempCourseRequirement);

            OptionalProgramRequirement tempOptionalProgramRule = null;

            handleOptionalProgramCourseMatchRule(tempCourseRequirement,tempOptionalProgramRule,requirementsMet,tempCourse,gradOptionalProgramRulesMatch);

            try {
                tempSC = objectMapper.readValue(objectMapper.writeValueAsString(tempCourse), StudentCourse.class);
                if (tempSC != null)
                    finalCourseList.add(tempSC);
                logger.debug("TempSC: {}", tempSC);
                logger.debug("Final course List size: : {}", finalCourseList.size());
                tempSPR = objectMapper.readValue(objectMapper.writeValueAsString(tempOptionalProgramRule),
                        OptionalProgramRequirement.class);
                if (tempSPR != null)
                    finalOptionalProgramRulesList.add(tempSPR);
                logger.debug("TempPR: {}", tempSPR);
                logger.debug("Final Program rules list size: {}", finalOptionalProgramRulesList.size());
            } catch (IOException e) {
                logger.error("ERROR:{}", e.getMessage());
            }
        }
        obj.setStudentCoursesOptionalProgram(finalCourseList);
        handleRule(gradOptionalProgramRulesMatch,finalOptionalProgramRulesList,requirementsNotMet,obj,requirementsMet);
    }
    public static void handleRule(List<OptionalProgramRequirement> gradOptionalProgramRulesMatch, List<OptionalProgramRequirement> finalOptionalProgramRulesList, List<GradRequirement> requirementsNotMet, OptionalProgramRuleProcessor obj, List<GradRequirement> requirementsMet) {
        if(gradOptionalProgramRulesMatch.size() != finalOptionalProgramRulesList.size()) {
            List<OptionalProgramRequirement> unusedRules = RuleEngineApiUtils.getCloneOptionalProgramRule(gradOptionalProgramRulesMatch);
            unusedRules.removeAll(finalOptionalProgramRulesList);
            finalOptionalProgramRulesList.addAll(unusedRules);
        }
        List<OptionalProgramRequirement> failedRules = finalOptionalProgramRulesList.stream().filter(pr -> !pr.getOptionalProgramRequirementCode().isPassed())
                .collect(Collectors.toList());

        handleFailedRules(failedRules,requirementsNotMet,obj);

        List<GradRequirement> resMet = obj.getRequirementsMetOptionalProgram();

        if (resMet == null)
            resMet = new ArrayList<>();

        resMet.addAll(requirementsMet);

        obj.setRequirementsMetOptionalProgram(resMet);
    }
}