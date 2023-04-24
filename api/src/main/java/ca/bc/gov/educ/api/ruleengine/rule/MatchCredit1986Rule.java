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

import java.util.*;
import java.util.stream.Collectors;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class MatchCredit1986Rule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(MatchCredit1986Rule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    public RuleData fire() {

        List<GradRequirement> requirementsMet = new ArrayList<>();
        List<GradRequirement> requirementsNotMet = new ArrayList<>();

        List<StudentCourse> courseList = RuleProcessorRuleUtils
                .getUniqueStudentCourses(ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());
        courseList.sort(Comparator.comparing(StudentCourse::getCourseLevel).reversed()
                .thenComparing(StudentCourse::getCompletedCoursePercentage).reversed());

        List<ProgramRequirement> gradProgramRulesMatch = ruleProcessorData.getGradProgramRules()
                .stream()
                .filter(gradProgramRule -> "M".compareTo(gradProgramRule.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
                        && "Y".compareTo(gradProgramRule.getProgramRequirementCode().getActiveRequirement()) == 0
                        && "C".compareTo(gradProgramRule.getProgramRequirementCode().getRequirementCategory()) == 0)
                .collect(Collectors.toList());

        if (courseList.isEmpty()) {
            logger.warn("!!!Empty list sent to Match Credit 1986 Rule for processing");
            return ruleProcessorData;
        }

        List<CourseRequirement> courseRequirements = ruleProcessorData.getCourseRequirements();
        if(courseRequirements == null) {
            courseRequirements = new ArrayList<>();
        }
        List<CourseRequirement> originalCourseRequirements = new ArrayList<>(courseRequirements);

        logger.debug("#### Match Program Rule size: {}",gradProgramRulesMatch.size());

        List<StudentCourse> finalCourseList = new ArrayList<>();
        List<ProgramRequirement> finalProgramRulesList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        for (StudentCourse tempCourse : courseList) {
            logger.debug("Processing Course: Code= {} Level = {}", tempCourse.getCourseCode(), tempCourse.getCourseLevel());
            logger.debug("Course Requirements size: {}", courseRequirements.size());
            if (tempCourse.getCourseCode().startsWith("X") && ruleProcessorData.getLdCounter() >= 8) {
                continue;
            }
            List<CourseRequirement> tempCourseRequirement = courseRequirements.stream()
                    .filter(cr -> tempCourse.getCourseCode().compareTo(cr.getCourseCode()) == 0
                            && tempCourse.getCourseLevel().compareTo(cr.getCourseLevel()) == 0)
                    .collect(Collectors.toList());

            logger.debug("Temp Course Requirement: {}", tempCourseRequirement);

            ProgramRequirement tempProgramRule = null;

            if (!tempCourseRequirement.isEmpty()) {
                for (CourseRequirement cr : tempCourseRequirement) {
                    if (tempProgramRule == null) {
                        tempProgramRule = gradProgramRulesMatch.stream()
                                .filter(pr -> pr.getProgramRequirementCode().getProReqCode().compareTo(cr.getRuleCode().getCourseRequirementCode()) == 0)
                                .findAny()
                                .orElse(null);
                    }
                }
            }
            logger.debug("Temp Program Rule: {}", tempProgramRule);
            processCourse(tempCourse, tempCourseRequirement, tempProgramRule, requirementsMet);

            AlgorithmSupportRule.copyAndAddIntoStudentCoursesList(tempCourse, finalCourseList, objectMapper);
            AlgorithmSupportRule.copyAndAddIntoProgramRulesList(tempProgramRule, finalProgramRulesList, objectMapper);
        }

        logger.debug("Final Program rules list: {}",finalProgramRulesList);
        processReqMetAndNotMet(finalProgramRulesList,requirementsNotMet,finalCourseList,originalCourseRequirements,requirementsMet,gradProgramRulesMatch);
        return ruleProcessorData;
    }
    private void processCourse(StudentCourse tempCourse, List<CourseRequirement> tempCourseRequirement, ProgramRequirement tempProgramRule, List<GradRequirement> requirementsMet) {
    	if (!tempCourseRequirement.isEmpty() && tempProgramRule != null) {
            if (requirementsMet.stream()
                    .filter(rm -> rm.getRule() != null && rm.getRule().equals(tempProgramRule.getProgramRequirementCode().getProReqCode()))
                    .findAny()
                    .orElse(null) == null) {
            	setDetailsForCourses(tempCourse,tempProgramRule,requirementsMet);
            } else {
                logger.debug("!!! Program Rule met Already: {}",tempProgramRule);
            }
        }
	}

	public void processReqMetAndNotMet(List<ProgramRequirement> finalProgramRulesList, List<GradRequirement> requirementsNotMet, List<StudentCourse> finalCourseList, List<CourseRequirement> originalCourseRequirements, List<GradRequirement> requirementsMet, List<ProgramRequirement> gradProgramRulesMatch) {
		if(gradProgramRulesMatch.size() != finalProgramRulesList.size()) {
            List<ProgramRequirement> unusedRules = RuleEngineApiUtils.getCloneProgramRule(gradProgramRulesMatch);
    		unusedRules.removeAll(finalProgramRulesList);
    		finalProgramRulesList.addAll(unusedRules);
    	}
        finalProgramRulesList.forEach(pr-> {
            if(pr.getProgramRequirementCode().getProReqCode().compareTo("745")==0 && !pr.getProgramRequirementCode().isPassed() && ruleProcessorData.getGradStatus().getConsumerEducationRequirementMet() != null && ruleProcessorData.getGradStatus().getConsumerEducationRequirementMet().compareTo("Y")==0) {
                pr.getProgramRequirementCode().setPassed(true);
                requirementsMet.add(new GradRequirement(pr.getProgramRequirementCode().getTraxReqNumber(), pr.getProgramRequirementCode().getLabel(),pr.getProgramRequirementCode().getProReqCode()));
            }
        });
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
                .filter(gradProgramRule -> "M".compareTo(gradProgramRule.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) != 0 || "C".compareTo(gradProgramRule.getProgramRequirementCode().getRequirementCategory()) != 0 || "4".compareTo(gradProgramRule.getProgramRequirementCode().getRequiredCredits()) != 0)
                .collect(Collectors.toList()));
       

        logger.debug("Final Program rules list size 2: {}",finalProgramRulesList.size());

        ruleProcessorData.setStudentCourses(finalCourseList);
        ruleProcessorData.setGradProgramRules(finalProgramRulesList);
        ruleProcessorData.setCourseRequirements(originalCourseRequirements);

        List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMet();

        if (reqsMet == null)
            reqsMet = new ArrayList<>();

        reqsMet.addAll(requirementsMet);
        ruleProcessorData.setRequirementsMet(reqsMet);
    }
    
    public void setDetailsForCourses(StudentCourse tempCourse, ProgramRequirement tempProgramRule, List<GradRequirement> requirementsMet) {
        if(tempCourse.getCourseCode().startsWith("X")) {
            Integer ldCounter = ruleProcessorData.getLdCounter();
            ruleProcessorData.setLdCounter(ldCounter + tempCourse.getCredits());
        }
        tempCourse.setUsed(true);
        tempCourse.setUsedInMatchRule(true);
        tempCourse.setCreditsUsedForGrad(tempCourse.getCredits());
        AlgorithmSupportRule.setGradReqMet(tempCourse,tempProgramRule);
        tempProgramRule.getProgramRequirementCode().setPassed(true);
        requirementsMet.add(new GradRequirement(tempProgramRule.getProgramRequirementCode().getTraxReqNumber(), tempProgramRule.getProgramRequirementCode().getLabel(),tempProgramRule.getProgramRequirementCode().getProReqCode()));
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.debug("MatchCredit1986Rule: Rule Processor Data set.");
    }

}
