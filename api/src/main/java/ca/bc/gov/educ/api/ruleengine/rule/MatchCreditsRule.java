package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.struct.*;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;

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

    public RuleData fire() {

        List<GradRequirement> requirementsMet = new ArrayList<>();
        List<GradRequirement> requirementsNotMet = new ArrayList<>();

        List<StudentCourse> courseList = ruleProcessorData.getStudentCourses();

        List<GradProgramRule> gradProgramRulesMatch = ruleProcessorData.getGradProgramRules()
                .stream()
                .filter(gradProgramRule -> "M".compareTo(gradProgramRule.getRequirementType()) == 0
                        && "Y".compareTo(gradProgramRule.getIsActive()) == 0
                        && "C".compareTo(gradProgramRule.getRuleCategory()) == 0)
                .collect(Collectors.toList());

        List<CourseRequirement> courseRequirements = ruleProcessorData.getCourseRequirements();
        List<CourseRequirement> originalCourseRequirements = new ArrayList<>(courseRequirements);

        logger.debug("#### Match Program Rule size: " + gradProgramRulesMatch.size());

        List<StudentCourse> finalCourseList = new ArrayList<>();
        List<GradProgramRule> finalProgramRulesList = new ArrayList<>();
        StudentCourse tempSC;
        GradProgramRule tempPR;
        ObjectMapper objectMapper = new ObjectMapper();

        ListIterator<StudentCourse> courseIterator = courseList.listIterator();

        while (courseIterator.hasNext()) {
            StudentCourse tempCourse = courseIterator.next();

            logger.debug("Processing Course: Code=" + tempCourse.getCourseCode() + " Level=" + tempCourse.getCourseLevel());
            logger.debug("Course Requirements size: " + courseRequirements.size());

            List<CourseRequirement> tempCourseRequirement = courseRequirements.stream()
                    .filter(cr -> tempCourse.getCourseCode().compareTo(cr.getCourseCode()) == 0
                            && tempCourse.getCourseLevel().compareTo(cr.getCourseLevel()) == 0)
                    .collect(Collectors.toList());

            logger.debug("Temp Course Requirement: " + tempCourseRequirement);

            GradProgramRule tempProgramRule = null;

            if (!tempCourseRequirement.isEmpty()) {
                for(CourseRequirement cr:tempCourseRequirement) {
                	if(tempProgramRule == null) {
	                	tempProgramRule = gradProgramRulesMatch.stream()
		                        .filter(pr -> pr.getRuleCode().compareTo(cr.getRuleCode()) == 0)
		                        .findAny()
		                        .orElse(null);
                	}
                }
            }
            logger.debug("Temp Program Rule: " + tempProgramRule);
            processCourse(tempCourse,tempCourseRequirement,tempProgramRule,requirementsMet,gradProgramRulesMatch);
            

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
        processReqMetAndNotMet(finalProgramRulesList,requirementsNotMet,finalCourseList,originalCourseRequirements,requirementsMet,gradProgramRulesMatch);        

        return ruleProcessorData;
    }
    
    private void processCourse(StudentCourse tempCourse, List<CourseRequirement> tempCourseRequirement, GradProgramRule tempProgramRule, List<GradRequirement> requirementsMet, List<GradProgramRule> gradProgramRulesMatch) {
    	if (!tempCourseRequirement.isEmpty() && tempProgramRule != null) {

            GradProgramRule finalTempProgramRule = tempProgramRule;
            if (requirementsMet.stream()
                    .filter(rm -> rm.getRule().equals(finalTempProgramRule.getRuleCode()))
                    .findAny()
                    .orElse(null) == null) {
            	setDetailsForCourses(tempCourse,tempProgramRule,requirementsMet,gradProgramRulesMatch,null);
            } else {
                logger.debug("!!! Program Rule met Already: " + tempProgramRule);
            }
        }else {
        	if(tempCourse.getCourseCode().startsWith("Y") 
        			&& tempCourse.getCourseLevel().contains("11")
        			&& (tempCourse.getFineArtsAppliedSkills().compareTo("B") == 0
        			|| tempCourse.getFineArtsAppliedSkills().compareTo("F") == 0
        			|| tempCourse.getFineArtsAppliedSkills().compareTo("A") == 0)) {
        		tempProgramRule = gradProgramRulesMatch.stream()
                        .filter(pr -> pr.getRuleCode().compareTo("111") == 0 && !pr.isPassed())
                        .findAny()
                        .orElse(null);
        		if(tempProgramRule != null) {
        			setDetailsForCourses(tempCourse,tempProgramRule,requirementsMet,gradProgramRulesMatch,"ExceptionalCase");
        		}
        	}
        }
		
	}

	public void processReqMetAndNotMet(List<GradProgramRule> finalProgramRulesList, List<GradRequirement> requirementsNotMet, List<StudentCourse> finalCourseList, List<CourseRequirement> originalCourseRequirements, List<GradRequirement> requirementsMet, List<GradProgramRule> gradProgramRulesMatch) {
    	List<GradProgramRule> unusedRules = null;
		if(gradProgramRulesMatch.size() != finalProgramRulesList.size()) {
    		unusedRules = RuleEngineApiUtils.getCloneProgramRule(gradProgramRulesMatch);
    		unusedRules.removeAll(finalProgramRulesList);
    		finalProgramRulesList.addAll(unusedRules);
    	}
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
                nonGradReasons = new ArrayList<>();

            nonGradReasons.addAll(requirementsNotMet);
            ruleProcessorData.setNonGradReasons(nonGradReasons);
        }

        //finalProgramRulesList only has the Match type rules in it. Add rest of the type of rules back to the list.
        finalProgramRulesList.addAll(ruleProcessorData.getGradProgramRules()
                .stream()
                .filter(gradProgramRule -> "M".compareTo(gradProgramRule.getRequirementType()) != 0 || "C".compareTo(gradProgramRule.getRuleCategory()) != 0)
                .collect(Collectors.toList()));
       

        logger.debug("Final Program rules list size 2: " + finalProgramRulesList.size());

        ruleProcessorData.setStudentCourses(finalCourseList);
        ruleProcessorData.setGradProgramRules(finalProgramRulesList);
        ruleProcessorData.setCourseRequirements(originalCourseRequirements);

        List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMet();

        if (reqsMet == null)
            reqsMet = new ArrayList<>();

        reqsMet.addAll(requirementsMet);
        ruleProcessorData.setRequirementsMet(reqsMet);
    }
    
    public void setDetailsForCourses(StudentCourse tempCourse, GradProgramRule tempProgramRule, List<GradRequirement> requirementsMet, List<GradProgramRule> gradProgramRulesMatch, String exceptionalCase) {
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
        if(exceptionalCase != null)
        	gradProgramRulesMatch.stream().filter(pr -> pr.getRuleCode().compareTo("111") == 0).forEach(pR -> pR.setPassed(true));
        requirementsMet.add(new GradRequirement(tempProgramRule.getRuleCode(), tempProgramRule.getRequirementName()));
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("MatchRule: Rule Processor Data set.");
    }

}
