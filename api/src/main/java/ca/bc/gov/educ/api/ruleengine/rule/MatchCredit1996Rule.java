package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
public class MatchCredit1996Rule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(MatchCredit1996Rule.class);
    private static final String RULE_CODE_732 = "10";
    private static final String RULE_CODE_726 = "8";
    private static final String RULE_CODE_727 = "9";
    private static final String FINE_ARTS = "F";
    private static final String APPLIED_SCIENCES = "A";
    private static final String FINE_ARTS_APPLIED_SCIENCES = "B";

    @Autowired
    private RuleProcessorData ruleProcessorData;

    public RuleData fire() {

        List<GradRequirement> requirementsMet = new ArrayList<>();
        List<GradRequirement> requirementsNotMet = new ArrayList<>();
        Map<String,Integer> map1996 = new HashMap<>();

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
            logger.warn("!!!Empty list sent to Match Credit 1996 Rule for processing");
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
                                .filter(pr -> pr.getProgramRequirementCode().getProReqCode().compareTo(cr.getRuleCode().getCourseRequirementCode()) == 0 && tempCourse.getCredits() >= Integer.parseInt(pr.getProgramRequirementCode().getRequiredCredits()))
                                .findAny()
                                .orElse(null);

                        if (tempProgramRule != null) {
                            ProgramRequirement finalTempProgramRule = tempProgramRule;
                            GradRequirement req = requirementsMet.stream().filter(rm -> rm.getRule().equals(finalTempProgramRule.getProgramRequirementCode().getTraxReqNumber())).findAny().orElse(null);
                            if (req != null) {
                                tempProgramRule = null;
                            }
                        }

                        if (tempProgramRule != null && tempCourse.getCredits() > Integer.parseInt(tempProgramRule.getProgramRequirementCode().getRequiredCredits())) {
                            int extraCredits = tempCourse.getCredits() - Integer.parseInt(tempProgramRule.getProgramRequirementCode().getRequiredCredits());
                            map1996.put(tempCourse.getCourseCode(), extraCredits);
                        }
                    }
                }
            }
            logger.debug("Temp Program Rule: {}", tempProgramRule);
            processCourse(tempCourse, tempCourseRequirement, tempProgramRule, requirementsMet, gradProgramRulesMatch, map1996);

            try {
                StudentCourse tempSC = objectMapper.readValue(objectMapper.writeValueAsString(tempCourse), StudentCourse.class);
                if (tempSC != null)
                    finalCourseList.add(tempSC);
                logger.debug("TempSC: {}", tempSC);
                logger.debug("Final course List size: {}: ", finalCourseList.size());
                ProgramRequirement tempPR = objectMapper.readValue(objectMapper.writeValueAsString(tempProgramRule), ProgramRequirement.class);
                if (tempPR != null && !finalProgramRulesList.contains(tempPR)) {
                    finalProgramRulesList.add(tempPR);
                }
                logger.debug("TempPR: {}", tempPR);
                logger.debug("Final Program rules list size: {}", finalProgramRulesList.size());
            } catch (IOException e) {
                logger.error("ERROR: {}", e.getMessage());
            }
        }

        logger.debug("Final Program rules list: {}",finalProgramRulesList);
        processReqMetAndNotMet(finalProgramRulesList,requirementsNotMet,finalCourseList,originalCourseRequirements,requirementsMet,gradProgramRulesMatch);        
        ruleProcessorData.setMap1996Crse(map1996);
        checkAppliedScienceAndFineArtsCondition(ruleProcessorData.getStudentCourses(),ruleProcessorData.getRequirementsMet(),ruleProcessorData.getNonGradReasons(),ruleProcessorData.getMap1996Crse());
        return ruleProcessorData;
    }

    private void checkAppliedScienceAndFineArtsCondition(List<StudentCourse> studentCourses, List<GradRequirement> requirementsMet, List<GradRequirement> nonGradReasons, Map<String, Integer> map1996Crse) {
        boolean reqmtSatisfied = false;
        int counter = 0; //counter to keep track of fine arts and applied science rule codes
        for(GradRequirement gR:requirementsMet) {
            if(gR.getRule().compareTo(RULE_CODE_732) == 0) {
                reqmtSatisfied= true;
                if(nonGradReasons != null) {
                    nonGradReasons.removeIf(e -> e.getRule() != null && e.getRule().compareTo(RULE_CODE_726) == 0);
                    nonGradReasons.removeIf(e -> e.getRule() != null && e.getRule().compareTo(RULE_CODE_727) == 0);
                }
            }
            if(gR.getRule().compareTo(RULE_CODE_726)==0 || gR.getRule().compareTo(RULE_CODE_727)==0) {
                counter++;
            }
        }
        if(counter ==2 && nonGradReasons != null)
            nonGradReasons.removeIf(e -> e.getRule() != null && e.getRule().compareTo(RULE_CODE_732) == 0);


        if(reqmtSatisfied) {
            requirementsMet.removeIf(e -> e.getRule() != null && e.getRule().compareTo(RULE_CODE_727) == 0);
            requirementsMet.removeIf(e -> e.getRule() != null && e.getRule().compareTo(RULE_CODE_726) == 0);

            for(StudentCourse sc:studentCourses) {
                if(sc.getGradReqMet().compareTo(RULE_CODE_727)==0 || sc.getGradReqMet().compareTo(RULE_CODE_726)==0){
                    sc.setGradReqMet("");
                    sc.setGradReqMetDetail("");
                    sc.setUsed(false);
                    sc.setUsedInMatchRule(false);

                    if(map1996Crse.get(sc.getCourseCode()) != null) {
                        map1996Crse.remove(sc.getCourseCode());
                    }

                }
            }
        }


    }

    private void processCourse(StudentCourse tempCourse, List<CourseRequirement> tempCourseRequirement, ProgramRequirement tempProgramRule, List<GradRequirement> requirementsMet, List<ProgramRequirement> gradProgramRulesMatch, Map<String,Integer> map1996) {
    	if (!tempCourseRequirement.isEmpty() && tempProgramRule != null) {
            ProgramRequirement finalTempProgramRule = tempProgramRule;
            if (requirementsMet.stream()
                    .filter(rm -> rm.getRule() != null && rm.getRule().equals(finalTempProgramRule.getProgramRequirementCode().getTraxReqNumber()))
                    .findAny()
                    .orElse(null) == null) {
            	setDetailsForCourses(tempCourse,tempProgramRule,requirementsMet);
            } else {
                logger.debug("!!! Program Rule met Already: {}",tempProgramRule);
            }
        }else {
            String ruleCode = "";
            if(tempCourse.getFineArtsAppliedSkills() != null) {
                if (tempCourse.getFineArtsAppliedSkills().compareTo(FINE_ARTS_APPLIED_SCIENCES) == 0) {
                    ruleCode = RULE_CODE_732;
                } else if (tempCourse.getFineArtsAppliedSkills().compareTo(FINE_ARTS) == 0) {
                    ruleCode = RULE_CODE_726;
                } else if (tempCourse.getFineArtsAppliedSkills().compareTo(APPLIED_SCIENCES) == 0) {
                    ruleCode = RULE_CODE_727;
                }
            }
            if(StringUtils.isNotBlank(ruleCode)) {
                String code = ruleCode;
                tempProgramRule = gradProgramRulesMatch.stream()
                        .filter(pr -> pr.getProgramRequirementCode().getProReqCode().compareTo(code) == 0 && !pr.getProgramRequirementCode().isPassed())
                        .findAny()
                        .orElse(null);
                if (tempProgramRule != null) {
                    if(tempCourse.getCredits() > Integer.parseInt(tempProgramRule.getProgramRequirementCode().getRequiredCredits())) {
                        int extraCredits = tempCourse.getCredits() - Integer.parseInt(tempProgramRule.getProgramRequirementCode().getRequiredCredits());
                        map1996.put(tempCourse.getCourseCode(),extraCredits);
                    }
                    setDetailsForCourses(tempCourse, tempProgramRule, requirementsMet);
                }
            }

        }
	}

	public void processReqMetAndNotMet(List<ProgramRequirement> finalProgramRulesList, List<GradRequirement> requirementsNotMet, List<StudentCourse> finalCourseList, List<CourseRequirement> originalCourseRequirements, List<GradRequirement> requirementsMet, List<ProgramRequirement> gradProgramRulesMatch) {
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
                requirementsNotMet.add(new GradRequirement(failedRule.getProgramRequirementCode().getTraxReqNumber(), failedRule.getProgramRequirementCode().getNotMetDesc()));
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
    	tempCourse.setUsed(true);
        tempCourse.setUsedInMatchRule(true);
        tempCourse.setCreditsUsedForGrad(tempCourse.getCredits());
        AlgorithmSupportRule.setGradReqMet(tempCourse,tempProgramRule);
        tempProgramRule.getProgramRequirementCode().setPassed(true);
        requirementsMet.add(new GradRequirement(tempProgramRule.getProgramRequirementCode().getTraxReqNumber(), tempProgramRule.getProgramRequirementCode().getLabel()));
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("Match4Credit1996Rule: Rule Processor Data set.");
    }

}
