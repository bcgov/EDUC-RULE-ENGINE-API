package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
public class MatchCredit1996Rule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(MatchCredit1996Rule.class);
    private static final String RULE_CODE_732 = "732";
    private static final String RULE_CODE_726 = "726";
    private static final String RULE_CODE_727 = "727";
    private static final String RULE_CODE_10 = "10";
    private static final String RULE_CODE_8 = "8";
    private static final String RULE_CODE_9 = "9";
    private static final String FINE_ARTS = "F";
    private static final String APPLIED_SCIENCES = "A";
    private static final String FINE_ARTS_APPLIED_SCIENCES = "B";

    @Autowired
    private RuleProcessorData ruleProcessorData;

    public RuleData fire() {

        List<GradRequirement> requirementsMet = new ArrayList<>();
        List<GradRequirement> requirementsNotMet = new ArrayList<>();

        List<StudentCourse> fineArtsCourseList = RuleProcessorRuleUtils
                .getUniqueStudentCourses(ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected())
                .stream().filter(studentCourse -> studentCourse.getFineArtsAppliedSkills() != null
                        && studentCourse.getFineArtsAppliedSkills().length() > 0)
                .collect(Collectors.toList());
        fineArtsCourseList.sort(Comparator.comparing(StudentCourse::getCourseLevel)
                .thenComparing(StudentCourse::getCompletedCoursePercentage, Comparator.reverseOrder()));

        List<StudentCourse> courseList = RuleProcessorRuleUtils
                .getUniqueStudentCourses(ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected())
                .stream().filter(studentCourse -> studentCourse.getFineArtsAppliedSkills() == null
                                || studentCourse.getFineArtsAppliedSkills().length() == 0)
                .collect(Collectors.toList());
        courseList.sort(Comparator.comparing(StudentCourse::getCourseLevel)
                .thenComparing(StudentCourse::getCompletedCoursePercentage, Comparator.reverseOrder()));

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
        

        for (StudentCourse tempCourse : fineArtsCourseList) {
            logger.debug("Processing Course: Code= {} Level = {}", tempCourse.getCourseCode(), tempCourse.getCourseLevel());
            logger.debug("Course Requirements size: {}", courseRequirements.size());

            List<CourseRequirement> tempCourseRequirements = courseRequirements.stream()
                    .filter(cr -> tempCourse.getCourseCode().compareTo(cr.getCourseCode()) == 0
                            && tempCourse.getCourseLevel().compareTo(cr.getCourseLevel()) == 0)
                    .collect(Collectors.toList());

            logger.debug("Temp Course Requirement: {}", tempCourseRequirements);

            ProgramRequirement tempProgramRule = null;

            if (!tempCourseRequirements.isEmpty()) {
                for (CourseRequirement cr : tempCourseRequirements) {
                    if (tempProgramRule == null) {
                        tempProgramRule = gradProgramRulesMatch.stream()
                                .filter(pr -> pr.getProgramRequirementCode().getProReqCode()
                                        .compareTo(cr.getRuleCode().getCourseRequirementCode()) == 0
                                        && tempCourse.getCredits() >= Integer.parseInt(pr.getProgramRequirementCode().getRequiredCredits())
                                        && isFineArtsOrAppliedSkillsRule(pr.getProgramRequirementCode().getProReqCode()))
                                .findAny()
                                .orElse(null);

                        if (tempProgramRule != null) {
                            ProgramRequirement finalTempProgramRule = tempProgramRule;
                            GradRequirement req = requirementsMet.stream()
                                    .filter(rm -> rm.getRule().equals(finalTempProgramRule.getProgramRequirementCode().getProReqCode()))
                                    .findAny().orElse(null);
                            if (req != null) {
                                tempProgramRule = null;
                            }
                        }

                        if (tempProgramRule != null
                                && tempCourse.getCredits() > Integer.parseInt(tempProgramRule.getProgramRequirementCode().getRequiredCredits())) {
                            int extraCredits = tempCourse.getCredits() - Integer.parseInt(tempProgramRule.getProgramRequirementCode().getRequiredCredits());
                            tempCourse.setLeftOverCredits(extraCredits);
                        }
                    }
                }
            }
            logger.debug("Temp Program Rule: {}", tempProgramRule);
            processCourse(tempCourse, tempCourseRequirements, tempProgramRule, requirementsMet, gradProgramRulesMatch);

            AlgorithmSupportRule.copyAndAddIntoStudentCoursesList(tempCourse, finalCourseList);
            AlgorithmSupportRule.copyAndAddIntoProgramRulesList(tempProgramRule, finalProgramRulesList);
        }
        logger.debug("Final Program rules list: {}",finalProgramRulesList);
        processReqMet(finalProgramRulesList,finalCourseList,originalCourseRequirements,requirementsMet,gradProgramRulesMatch);

        for (StudentCourse tempCourse : courseList) {
            logger.debug("Processing Course: Code= {} Level = {}", tempCourse.getCourseCode(), tempCourse.getCourseLevel());
            logger.debug("Course Requirements size: {}", courseRequirements.size());

            List<CourseRequirement> tempCourseRequirements = courseRequirements.stream()
                    .filter(cr -> tempCourse.getCourseCode().compareTo(cr.getCourseCode()) == 0
                            && tempCourse.getCourseLevel().compareTo(cr.getCourseLevel()) == 0)
                    .collect(Collectors.toList());

            logger.debug("Temp Course Requirement: {}", tempCourseRequirements);

            ProgramRequirement tempProgramRule = null;

            if (!tempCourseRequirements.isEmpty()) {
                for (CourseRequirement cr : tempCourseRequirements) {
                    if (tempProgramRule == null) {
                        tempProgramRule = gradProgramRulesMatch.stream()
                                .filter(pr -> pr.getProgramRequirementCode().getProReqCode().compareTo(cr.getRuleCode().getCourseRequirementCode()) == 0
                                        && tempCourse.getCredits() >= Integer.parseInt(pr.getProgramRequirementCode().getRequiredCredits())
                                        && !pr.getProgramRequirementCode().isPassed())
                                .findAny()
                                .orElse(null);

                        if (tempProgramRule != null) {
                            ProgramRequirement finalTempProgramRule = tempProgramRule;
                            GradRequirement req = requirementsMet.stream().filter(rm -> rm.getRule().equals(finalTempProgramRule.getProgramRequirementCode().getProReqCode())).findAny().orElse(null);
                            if (req != null) {
                                tempProgramRule = null;
                            }
                        }

                        if (tempProgramRule != null && tempCourse.getCredits() > Integer.parseInt(tempProgramRule.getProgramRequirementCode().getRequiredCredits())) {
                            int extraCredits = tempCourse.getCredits() - Integer.parseInt(tempProgramRule.getProgramRequirementCode().getRequiredCredits());
                            tempCourse.setLeftOverCredits(extraCredits);
                        }
                    }
                }
            }
            logger.debug("Temp Program Rule: {}", tempProgramRule);
            processCourse(tempCourse, tempCourseRequirements, tempProgramRule, requirementsMet, gradProgramRulesMatch);

            AlgorithmSupportRule.copyAndAddIntoStudentCoursesList(tempCourse, finalCourseList);
            AlgorithmSupportRule.copyAndAddIntoProgramRulesList(tempProgramRule, finalProgramRulesList);
        }

        logger.debug("Final Program rules list: {}",finalProgramRulesList);
        processReqMetAndNotMet(finalProgramRulesList,requirementsNotMet,finalCourseList,originalCourseRequirements,requirementsMet,gradProgramRulesMatch);        
        checkAppliedScienceAndFineArtsCondition(ruleProcessorData.getStudentCourses(),ruleProcessorData.getRequirementsMet(),ruleProcessorData.getNonGradReasons());
        return ruleProcessorData;
    }

    private void checkAppliedScienceAndFineArtsCondition(List<StudentCourse> studentCourses, List<GradRequirement> requirementsMet, List<GradRequirement> nonGradReasons) {
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
                if(sc.getGradReqMet().compareTo(RULE_CODE_9)==0 || sc.getGradReqMet().compareTo(RULE_CODE_8)==0){
                    sc.setGradReqMet("");
                    sc.setGradReqMetDetail("");
                    sc.setUsed(false);
                    sc.setUsedInMatchRule(false);

                    if (sc.getLeftOverCredits() != null && sc.getLeftOverCredits() > 0) {
                        sc.setLeftOverCredits(null);
                    }
                }
            }
        }
    }

    private void processCourse(StudentCourse tempCourse, List<CourseRequirement> tempCourseRequirements, ProgramRequirement tempProgramRule, List<GradRequirement> requirementsMet, List<ProgramRequirement> gradProgramRulesMatch) {
    	if (!tempCourseRequirements.isEmpty() && tempProgramRule != null) {
            ProgramRequirement finalTempProgramRule = tempProgramRule;
            if (requirementsMet.stream()
                    .filter(rm -> rm.getRule() != null && rm.getRule().equals(finalTempProgramRule.getProgramRequirementCode().getProReqCode()))
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
                        tempCourse.setLeftOverCredits(extraCredits);
                    }
                    setDetailsForCourses(tempCourse, tempProgramRule, requirementsMet);
                }
            }
        }
	}

    public void processReqMet(List<ProgramRequirement> finalProgramRulesList, List<StudentCourse> finalCourseList, List<CourseRequirement> originalCourseRequirements, List<GradRequirement> requirementsMet, List<ProgramRequirement> gradProgramRulesMatch) {
        if(gradProgramRulesMatch.size() != finalProgramRulesList.size()) {
            List<ProgramRequirement> unusedRules = RuleEngineApiUtils.getCloneProgramRule(gradProgramRulesMatch);
            unusedRules.removeAll(finalProgramRulesList);
            finalProgramRulesList.addAll(unusedRules);
        }

        //finalProgramRulesList only has the Match type rules in it. Add rest of the type of rules back to the list.
        finalProgramRulesList.addAll(ruleProcessorData.getGradProgramRules()
                .stream()
                .filter(gradProgramRule -> "M".compareTo(gradProgramRule.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) != 0
                        || "C".compareTo(gradProgramRule.getProgramRequirementCode().getRequirementCategory()) != 0)
                .collect(Collectors.toList()));

        logger.debug("Final Program rules list size 2: {}", finalProgramRulesList.size());

        ruleProcessorData.setStudentCourses(finalCourseList);
        ruleProcessorData.setGradProgramRules(finalProgramRulesList);
        ruleProcessorData.setCourseRequirements(originalCourseRequirements);

        List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMet();

        if (reqsMet == null)
            reqsMet = new ArrayList<>();

        reqsMet.addAll(requirementsMet);
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
                .filter(gradProgramRule -> "M".compareTo(gradProgramRule.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) != 0
                        || "C".compareTo(gradProgramRule.getProgramRequirementCode().getRequirementCategory()) != 0)
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
        tempCourse.setCreditsUsedForGrad(tempCourse.getLeftOverCredits() != null && tempCourse.getLeftOverCredits() > 0? tempCourse.getLeftOverCredits():tempCourse.getCredits());
        AlgorithmSupportRule.setGradReqMet(tempCourse,tempProgramRule);
        tempProgramRule.getProgramRequirementCode().setPassed(true);
        requirementsMet.add(new GradRequirement(tempProgramRule.getProgramRequirementCode().getTraxReqNumber(), tempProgramRule.getProgramRequirementCode().getLabel(),tempProgramRule.getProgramRequirementCode().getProReqCode()));
    }

    private boolean isFineArtsOrAppliedSkillsRule(String programRequirementCode) {
        return RULE_CODE_726.compareTo(programRequirementCode) == 0
                || RULE_CODE_727.compareTo(programRequirementCode) == 0
                || RULE_CODE_732.compareTo(programRequirementCode) == 0;
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
    }
}
