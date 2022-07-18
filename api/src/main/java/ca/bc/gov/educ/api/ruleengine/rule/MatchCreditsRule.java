package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;

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
import java.util.*;
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

        List<StudentCourse> courseList = RuleProcessorRuleUtils.getUniqueStudentCourses(
                ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());

        List<ProgramRequirement> gradProgramRulesMatch = ruleProcessorData.getGradProgramRules()
                .stream()
                .filter(gradProgramRule -> "M".compareTo(gradProgramRule.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
                        && "Y".compareTo(gradProgramRule.getProgramRequirementCode().getActiveRequirement()) == 0
                        && "C".compareTo(gradProgramRule.getProgramRequirementCode().getRequirementCategory()) == 0)
                .collect(Collectors.toList());

        if (courseList == null || courseList.isEmpty()) {
            logger.warn("!!!Empty list sent to Match Credits Rule for processing");
            return ruleProcessorData;
        }

        List<CourseRequirement> courseRequirements = ruleProcessorData.getCourseRequirements();
        if(courseRequirements == null) {
            courseRequirements = new ArrayList<>();
        }
        List<CourseRequirement> originalCourseRequirements = new ArrayList<>(courseRequirements);

        List<StudentCourse> finalCourseList = new ArrayList<>();
        List<ProgramRequirement> finalProgramRulesList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        ListIterator<StudentCourse> courseIterator = courseList.listIterator();
        Map<String,Integer> courseCreditException = new HashMap<>();
        while (courseIterator.hasNext()) {
            StudentCourse tempCourse = courseIterator.next();

            List<CourseRequirement> tempCourseRequirement = courseRequirements.stream()
                    .filter(cr -> tempCourse.getCourseCode().compareTo(cr.getCourseCode()) == 0
                            && tempCourse.getCourseLevel().compareTo(cr.getCourseLevel()) == 0)
                    .collect(Collectors.toList());

            ProgramRequirement tempProgramRule = null;

            if (!tempCourseRequirement.isEmpty()) {
                for(CourseRequirement cr:tempCourseRequirement) {
                	if(tempProgramRule == null) {
	                	tempProgramRule = gradProgramRulesMatch.stream()
		                        .filter(pr -> pr.getProgramRequirementCode().getProReqCode().compareTo(cr.getRuleCode().getCourseRequirementCode()) == 0)
		                        .findAny()
		                        .orElse(null);
                	}
                }
            }
            processCourse(tempCourse,tempCourseRequirement,tempProgramRule,requirementsMet,gradProgramRulesMatch,courseCreditException);

            try {
                StudentCourse tempSC = objectMapper.readValue(objectMapper.writeValueAsString(tempCourse), StudentCourse.class);
                if (tempSC != null)
                    finalCourseList.add(tempSC);
                ProgramRequirement tempPR = objectMapper.readValue(objectMapper.writeValueAsString(tempProgramRule), ProgramRequirement.class);
                if (tempPR != null && !finalProgramRulesList.contains(tempPR)) {
                    finalProgramRulesList.add(tempPR);
                }

            } catch (IOException e) {
                logger.error("ERROR: {}",e.getMessage());
            }
        }
        processReqMetAndNotMet(finalProgramRulesList,requirementsNotMet,finalCourseList,originalCourseRequirements,requirementsMet,gradProgramRulesMatch);        

        return ruleProcessorData;
    }
    
    private void processCourse(StudentCourse tempCourse, List<CourseRequirement> tempCourseRequirement, ProgramRequirement tempProgramRule, List<GradRequirement> requirementsMet, List<ProgramRequirement> gradProgramRulesMatch,Map<String,Integer> courseCreditException) {
    	if (!tempCourseRequirement.isEmpty() && tempProgramRule != null) {

            ProgramRequirement finalTempProgramRule = tempProgramRule;
            if (requirementsMet.stream()
                    .filter(rm -> rm.getRule() != null && rm.getRule().equals(finalTempProgramRule.getProgramRequirementCode().getProReqCode()))
                    .findAny()
                    .orElse(null) == null) {
            	setDetailsForCourses(tempCourse,tempProgramRule,requirementsMet,gradProgramRulesMatch,null,courseCreditException);
            } else {
                logger.debug("!!! Program Rule met Already: {}",tempProgramRule);
            }
        }else {
        	if(tempCourse.getCourseCode().startsWith("Y") 
        			&& tempCourse.getCourseLevel().contains("11")
        			&& (tempCourse.getFineArtsAppliedSkills().compareTo("B") == 0
        			|| tempCourse.getFineArtsAppliedSkills().compareTo("F") == 0
        			|| tempCourse.getFineArtsAppliedSkills().compareTo("A") == 0)) {
        		tempProgramRule = gradProgramRulesMatch.stream()
                        .filter(pr -> (pr.getProgramRequirementCode().getProReqCode().compareTo("111") == 0 || pr.getProgramRequirementCode().getProReqCode().compareTo("712") == 0) && !pr.getProgramRequirementCode().isPassed())
                        .findAny()
                        .orElse(null);
        		if(tempProgramRule != null) {
        			setDetailsForCourses(tempCourse,tempProgramRule,requirementsMet,gradProgramRulesMatch,"ExceptionalCase",courseCreditException);
        		}
        	}
        }
		
	}

	public void processReqMetAndNotMet(List<ProgramRequirement> finalProgramRulesList, List<GradRequirement> requirementsNotMet, List<StudentCourse> finalCourseList, List<CourseRequirement> originalCourseRequirements, List<GradRequirement> requirementsMet, List<ProgramRequirement> gradProgramRulesMatch) {
    	finalProgramRulesList.removeIf(e -> e.getProgramRequirementCode().isTempFailed());
		if(gradProgramRulesMatch.size() != finalProgramRulesList.size()) {
            List<ProgramRequirement> unusedRules = RuleEngineApiUtils.getCloneProgramRule(gradProgramRulesMatch);
    		unusedRules.removeAll(finalProgramRulesList);
    		finalProgramRulesList.addAll(unusedRules);
    	}
		List<ProgramRequirement> failedRules = finalProgramRulesList.stream()
                .filter(pr -> !pr.getProgramRequirementCode().isPassed()).collect(Collectors.toList());

        if (failedRules.isEmpty()) {
            logger.debug("All the match rules met!");
        } else {
            for (ProgramRequirement failedRule : failedRules) {
                requirementsNotMet.add(new GradRequirement(failedRule.getProgramRequirementCode().getTraxReqNumber(), failedRule.getProgramRequirementCode().getNotMetDesc(),failedRule.getProgramRequirementCode().getProReqCode()));
            }

            logger.debug("One or more Match rules not met!");
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
                .filter(gradProgramRule -> "M".compareTo(gradProgramRule.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) != 0 || "C".compareTo(gradProgramRule.getProgramRequirementCode().getRequirementCategory()) != 0)
                .collect(Collectors.toList()));

        ruleProcessorData.setStudentCourses(finalCourseList);
        ruleProcessorData.setGradProgramRules(finalProgramRulesList);
        ruleProcessorData.setCourseRequirements(originalCourseRequirements);

        List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMet();

        if (reqsMet == null)
            reqsMet = new ArrayList<>();

        reqsMet.addAll(requirementsMet);
        ruleProcessorData.setRequirementsMet(reqsMet);
    }
    
    public void setDetailsForCourses(StudentCourse tempCourse, ProgramRequirement tempProgramRule, List<GradRequirement> requirementsMet, List<ProgramRequirement> gradProgramRulesMatch, String exceptionalCase,Map<String,Integer> courseCreditException) {
    	tempCourse.setUsed(true);
        tempCourse.setUsedInMatchRule(true);
        if(courseCreditException.get(tempProgramRule.getProgramRequirementCode().getProReqCode()) == null) {
            tempCourse.setCreditsUsedForGrad(tempCourse.getCredits());
        }else {
            int leftOverCredits = tempCourse.getCredits() - courseCreditException.get(tempProgramRule.getProgramRequirementCode().getProReqCode());
            tempCourse.setCreditsUsedForGrad(leftOverCredits != 0?leftOverCredits:tempCourse.getCredits());
            tempCourse.setLeftOverCredits(leftOverCredits);
        }
        AlgorithmSupportRule.setGradReqMet(tempCourse,tempProgramRule);
        if(tempCourse.getCreditsUsedForGrad() == 2) {
            tempProgramRule.getProgramRequirementCode().setTempFailed(true);

            courseCreditException.merge(tempProgramRule.getProgramRequirementCode().getProReqCode(), 2, Integer::sum);
        }
        if(exceptionalCase != null)
            gradProgramRulesMatch.stream().filter(pr -> pr.getProgramRequirementCode().getProReqCode().compareTo("111") == 0).forEach(pR -> pR.getProgramRequirementCode().setPassed(true));

        if(courseCreditException.get(tempProgramRule.getProgramRequirementCode().getProReqCode()) == null) {
            tempProgramRule.getProgramRequirementCode().setPassed(true);
            requirementsMet.add(new GradRequirement(tempProgramRule.getProgramRequirementCode().getTraxReqNumber(), tempProgramRule.getProgramRequirementCode().getLabel(),tempProgramRule.getProgramRequirementCode().getProReqCode()));
        }else {
            if(courseCreditException.get(tempProgramRule.getProgramRequirementCode().getProReqCode()) == 4) {
                tempProgramRule.getProgramRequirementCode().setPassed(true);
                tempProgramRule.getProgramRequirementCode().setTempFailed(false);
                requirementsMet.add(new GradRequirement(tempProgramRule.getProgramRequirementCode().getTraxReqNumber(), tempProgramRule.getProgramRequirementCode().getLabel(),tempProgramRule.getProgramRequirementCode().getProReqCode()));
            }
        }
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("MatchRule: Rule Processor Data set.");
    }

}
