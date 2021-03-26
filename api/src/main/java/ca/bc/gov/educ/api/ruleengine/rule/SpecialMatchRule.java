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
import ca.bc.gov.educ.api.ruleengine.struct.GradRequirement;
import ca.bc.gov.educ.api.ruleengine.struct.GradSpecialProgramRule;
import ca.bc.gov.educ.api.ruleengine.struct.RuleData;
import ca.bc.gov.educ.api.ruleengine.struct.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.struct.StudentCourse;
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
    private RuleProcessorData ruleProcessorData;
    
    final RuleType ruleType = RuleType.MATCH;

    public RuleData fire() {

        List<GradRequirement> requirementsMet = new ArrayList<GradRequirement>();
        List<GradRequirement> requirementsNotMet = new ArrayList<GradRequirement>();

        List<StudentCourse> courseList = ruleProcessorData.getStudentCourses();
        List<GradSpecialProgramRule> gradSpecialProgramRulesMatch = ruleProcessorData.getGradSpecialProgramRules()
                .stream()
                .filter(gradSpecialProgramRule -> "M".compareTo(gradSpecialProgramRule.getRequirementType()) == 0)
                .collect(Collectors.toList());
        List<CourseRequirement> courseRequirements = ruleProcessorData.getCourseRequirements();
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
        
        
        ruleProcessorData.setStudentCourses(finalCourseList);
        ruleProcessorData.setGradSpecialProgramRules(finalSpecialProgramRulesList);
        ruleProcessorData.setCourseRequirements(originalCourseRequirements);

        //logger.debug("Output Data:\n" + outputData);

        List<GradSpecialProgramRule> failedRules = finalSpecialProgramRulesList.stream()
                .filter(pr -> !pr.isPassed()).collect(Collectors.toList());

        if (failedRules.isEmpty()) {
            logger.debug("All the match rules met!");
        } else {
            for (GradSpecialProgramRule failedRule : failedRules) {
                requirementsNotMet.add(new GradRequirement(failedRule.getRuleCode(), failedRule.getNotMetDesc()));
            }
            logger.debug("One or more Match rules not met!");
        }

        ruleProcessorData.setRequirementsMet(requirementsMet);
        ruleProcessorData.setNonGradReasons(requirementsNotMet);

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

        return ruleProcessorData;
    }


    public boolean fire(Object inputData, Object outputData) {
        return false;
    }
    
    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("SpecialMatchRule: Rule Processor Data set.");
    }

}
