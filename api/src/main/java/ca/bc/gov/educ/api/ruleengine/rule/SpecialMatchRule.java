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

import ca.bc.gov.educ.api.ruleengine.struct.CourseRequirement;
import ca.bc.gov.educ.api.ruleengine.struct.CourseRequirements;
import ca.bc.gov.educ.api.ruleengine.struct.GradRequirement;
import ca.bc.gov.educ.api.ruleengine.struct.GradSpecialProgramRule;
import ca.bc.gov.educ.api.ruleengine.struct.GradSpecialProgramRules;
import ca.bc.gov.educ.api.ruleengine.struct.SpecialMatchRuleData;
import ca.bc.gov.educ.api.ruleengine.struct.RuleData;
import ca.bc.gov.educ.api.ruleengine.struct.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.struct.StudentCourses;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class SpecialMatchRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(SpecialMatchRule.class);

    @Autowired
    private SpecialMatchRuleData inputData;
    final RuleType ruleType = RuleType.MATCH;

    public RuleData fire() {

        List<GradRequirement> requirementsMet = new ArrayList<GradRequirement>();
        List<GradRequirement> requirementsNotMet = new ArrayList<GradRequirement>();

        List<StudentCourse> courseList = inputData.getStudentCourses().getStudentCourseList();
        List<GradSpecialProgramRule> gradSpecialProgramRulesMatch = inputData.getGradSpecialProgramRules().getGradProgramRuleList()
                .stream()
                .filter(gradSpecialProgramRule -> "M".compareTo(gradSpecialProgramRule.getRequirementType()) == 0)
                .collect(Collectors.toList());
        List<CourseRequirement> courseRequirements = inputData.getCourseRequirements().getCourseRequirementList();
        List<CourseRequirement> originalCourseRequirements = new ArrayList<CourseRequirement>(courseRequirements);
       
        logger.debug("#### Match Special Program Rule size: " + gradSpecialProgramRulesMatch.size());

        ListIterator<StudentCourse> courseIterator = courseList.listIterator();
       
        List<StudentCourse> finalCourseList = new ArrayList<StudentCourse>();
        List<GradSpecialProgramRule> finalSpecialProgramRulesList = new ArrayList<GradSpecialProgramRule>();
        StudentCourse tempSC;
        GradSpecialProgramRule tempSPR;
        ObjectMapper objectMapper = new ObjectMapper();

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

            GradSpecialProgramRule tempSpecialProgramRule = null;

            if (tempCourseRequirement != null) {
            	tempSpecialProgramRule = gradSpecialProgramRulesMatch.stream()
                        .filter(pr -> pr.getRuleCode().compareTo(tempCourseRequirement.getRuleCode()) == 0)
                        .findAny()
                        .orElse(null);
            }            
            logger.debug("Temp Program Rule: " + tempSpecialProgramRule);

            if (tempCourseRequirement != null && tempSpecialProgramRule != null) {

            	GradSpecialProgramRule finalTempProgramRule = tempSpecialProgramRule;
                if (requirementsMet.stream()
                        .filter(rm -> rm.getRule() == finalTempProgramRule.getRuleCode())
                        .findAny().orElse(null) == null) {
                    tempCourse.setUsed(true);
                    tempCourse.setCreditsUsedForGrad(tempCourse.getCredits());

                    if (tempCourse.getGradReqMet().length() > 0) {

                        tempCourse.setGradReqMet(tempCourse.getGradReqMet() + ", " + tempSpecialProgramRule.getRuleCode());
                        tempCourse.setGradReqMetDetail(tempCourse.getGradReqMetDetail() + ", " + tempSpecialProgramRule.getRuleCode()
                                + " - " + tempSpecialProgramRule.getRequirementName());
                    } else {
                        tempCourse.setGradReqMet(tempSpecialProgramRule.getRuleCode());
                        tempCourse.setGradReqMetDetail(tempSpecialProgramRule.getRuleCode() + " - " + tempSpecialProgramRule.getRequirementName());
                    }

                    tempSpecialProgramRule.setPassed(true);
                    requirementsMet.add(new GradRequirement(tempSpecialProgramRule.getRuleCode(), tempSpecialProgramRule.getRequirementName()));
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
                tempSPR = objectMapper.readValue(objectMapper.writeValueAsString(tempSpecialProgramRule), GradSpecialProgramRule.class);
                if (tempSPR != null)
                	finalSpecialProgramRulesList.add(tempSPR);
                logger.debug("TempPR: " + tempSPR);
                logger.debug("Final Program rules list size: " + finalSpecialProgramRulesList.size());
            } catch (IOException e) {
                logger.error("ERROR:" + e.getMessage());
            }
        }
        
        

        SpecialMatchRuleData outputData = new SpecialMatchRuleData();
        StudentCourses studentCourses = new StudentCourses();
        GradSpecialProgramRules programRules = new GradSpecialProgramRules();
        CourseRequirements courseReqs = new CourseRequirements();
        
        studentCourses.setStudentCourseList(finalCourseList);
        programRules.setGradProgramRuleList(finalSpecialProgramRulesList);
        courseReqs.setCourseRequirementList(originalCourseRequirements);
        outputData.setStudentCourses(studentCourses);
        outputData.setGradSpecialProgramRules(programRules);
        outputData.setCourseRequirements(courseReqs);

        //logger.debug("Output Data:\n" + outputData);

        List<GradSpecialProgramRule> failedRules = finalSpecialProgramRulesList.stream()
                .filter(pr -> !pr.isPassed()).collect(Collectors.toList());

        if (failedRules.isEmpty()) {
            outputData.setPassed(true);
            logger.debug("All the match rules met!");
        } else {
            for (GradSpecialProgramRule failedRule : failedRules) {
                requirementsNotMet.add(new GradRequirement(failedRule.getRuleCode(), failedRule.getNotMetDesc()));
            }
            logger.debug("One or more Match rules not met!");
        }

        outputData.setPassMessages(requirementsMet);
        outputData.setFailMessages(requirementsNotMet);

        /*ListIterator<AchievementDto> achievementsIterator = achievementsCopy.listIterator();

        while(achievementsIterator.hasNext()) {

            AchievementDto tempAchievement = achievementsIterator.next();
            ProgramRule tempProgramRule = programRulesMatch.stream()
                    .filter(pr -> tempAchievement
                            .getCourse()
                            .getRequirementCode()
                            .getRequirementCode() == pr.getRequirementCode())
                    .findAny()
                    .orElse(null);

            if(tempProgramRule != null && !tempAchievement.isFailed() && !tempAchievement.isDuplicate()){
                achievementsIterator.remove();
                logger.debug("Requirement Met -> Requirement Code:" + tempProgramRule.getRequirementCode()
                        + " Course:" + tempAchievement.getCourse().getCourseName() + "\n");

                tempAchievement.setGradRequirementMet(tempProgramRule.getRequirementCode());
                student.getRequirementsMet().add("Met " + tempProgramRule.getRequirementName());
                finalAchievements.add(tempAchievement);
                programRulesMatch.remove(tempProgramRule);
                achievementsCopy.remove(tempAchievement);
            }
        }

        finalAchievements = Stream.concat(finalAchievements.stream()
                , achievementsCopy.stream())
                .collect(Collectors.toList());

        student.setAchievements(finalAchievements);

        logger.debug("Leftover Course Achievements:" + achievementsCopy + "\n");
        logger.debug("Leftover Program Rules: " + programRulesMatch + "\n");

        if (programRulesMatch.size() > 0) {
            gradStatusFlag = false;

            for (ProgramRule programRule : programRulesMatch) {
                student.getRequirementsNotMet().add(programRule.getNotMetDescription());
            }

            student.getGradMessages().add("All the Match rules not Met.");
        }
        else {
            student.getGradMessages().add("All the Match rules met.");
        }*/

        return outputData;
    }


    public boolean fire(Object inputData, Object outputData) {
        return false;
    }

}
