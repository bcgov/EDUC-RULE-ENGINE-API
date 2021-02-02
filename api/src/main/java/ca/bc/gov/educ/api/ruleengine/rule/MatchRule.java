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
public class MatchRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(MatchRule.class);

    @Autowired
    private MatchRuleData inputData;
    final RuleType ruleType = RuleType.MATCH;

    public RuleData fire() {

        List<GradRequirement> requirementsMet = new ArrayList<GradRequirement>();
        List<GradRequirement> requirementsNotMet = new ArrayList<GradRequirement>();

        List<StudentCourse> courseList = inputData.getStudentCourses().getStudentCourseList();
        List<StudentCourse> originalCourseList = new ArrayList<StudentCourse>(courseList);

        List<ProgramRule> programRulesMatch = inputData.getProgramRules().getProgramRuleList()
                .stream()
                .filter(programRule -> "M".compareTo(programRule.getRequirementType()) == 0)
                .collect(Collectors.toList());
        List<ProgramRule> programRulesMatchOriginal = new ArrayList<ProgramRule>(programRulesMatch);

        List<CourseRequirement> courseRequirements = inputData.getCourseRequirements().getCourseRequirementList();
        List<CourseRequirement> originalCourseRequirements = new ArrayList<CourseRequirement>(courseRequirements);

        //logger.debug("Course Requirements: " + courseRequirements);
        logger.debug("#### Match Program Rule size: " + programRulesMatch.size());

        ListIterator<StudentCourse> courseIterator = courseList.listIterator();

        List<StudentCourse> finalCourseList = new ArrayList<StudentCourse>();
        List<ProgramRule> finalProgramRulesList = new ArrayList<ProgramRule>();
        StudentCourse tempSC;
        ProgramRule tempPR;
        ObjectMapper objectMapper = new ObjectMapper();

        while (courseIterator.hasNext()) {
            StudentCourse tempCourse = courseIterator.next();

            logger.debug("Processing Course: Code=" + tempCourse.getCourseCode() + " Level=" + tempCourse.getCourseLevel());
            logger.debug("Course Requirements size: " + courseRequirements.size());

            CourseRequirement tempCourseRequirement = courseRequirements.stream()
                    .filter(cr -> tempCourse.getCourseCode().compareTo(cr.getCourseCode()) == 0
                            && tempCourse.getCourseLevel().compareTo(cr.getCourseLevel()) == 0 )
                    .findAny()
                    .orElse(null);

            logger.debug("Temp Course Requirement: " + tempCourseRequirement);

            ProgramRule tempProgramRule = null;

            if (tempCourseRequirement != null) {
                tempProgramRule = programRulesMatch.stream()
                        .filter(pr -> pr.getCode().compareTo(tempCourseRequirement.getRuleCode()) == 0)
                        .findAny()
                        .orElse(null);
                logger.debug("ALERT: Inconsistent Data Found. " + tempCourseRequirement.getRuleCode() +
                        " Exists in the CourseRequirments but doesn't exist in ProgramRules");
            }
            logger.debug("Temp Program Rule: " + tempProgramRule);

            if (tempCourseRequirement != null && tempProgramRule != null) {
                tempCourse.setUsed(true);
                tempCourse.setGradReqMet(tempCourse.getGradReqMet() + " " + tempProgramRule.getCode());
                tempProgramRule.setPassed(true);
                requirementsMet.add(new GradRequirement(tempProgramRule.getCode(), tempProgramRule.getRequirementName()));
            }

            tempSC = new StudentCourse();
            tempPR = new ProgramRule();
            try {
                tempSC = objectMapper.readValue(objectMapper.writeValueAsString(tempCourse), StudentCourse.class);
                if (tempSC != null)
                    finalCourseList.add(tempSC);
                logger.debug("TempSC: " + tempSC);
                logger.debug("Final course List size: : " + finalCourseList.size());
                tempPR = objectMapper.readValue(objectMapper.writeValueAsString(tempProgramRule), ProgramRule.class);
                if (tempPR != null)
                    finalProgramRulesList.add(tempPR);
                logger.debug("TempPR: " + tempPR);
                logger.debug("Final Program rules list size: " + finalProgramRulesList.size());
            } catch (IOException e) {
                logger.error("ERROR:" + e.getMessage());
            }
        }

        MatchRuleData outputData = new MatchRuleData();
        StudentCourses studentCourses = new StudentCourses();
        ProgramRules programRules = new ProgramRules();
        CourseRequirements courseReqs = new CourseRequirements();

        studentCourses.setStudentCourseList(finalCourseList);
        programRules.setProgramRuleList(finalProgramRulesList);
        courseReqs.setCourseRequirementList(originalCourseRequirements);

        outputData.setStudentCourses(studentCourses);
        outputData.setProgramRules(programRules);
        outputData.setCourseRequirements(courseReqs);

        //logger.debug("Output Data:\n" + outputData);

        List<ProgramRule> failedRules = finalProgramRulesList.stream()
                .filter(pr -> !pr.isPassed()).collect(Collectors.toList());

        if (failedRules.isEmpty()) {
            outputData.setPassed(true);
            logger.debug("All the match rules met!");
        } else {
            for (ProgramRule failedRule : failedRules) {
                requirementsNotMet.add(new GradRequirement(failedRule.getCode(), failedRule.getNotMetDescription()));
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
