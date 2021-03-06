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
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentsMatchCreditsRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(AssessmentsMatchCreditsRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    public RuleData fire() {

        List<GradRequirement> requirementsMet = new ArrayList<>();
        List<GradRequirement> requirementsNotMet = new ArrayList<>();
        List<StudentCourse> courseList = RuleProcessorRuleUtils.getUniqueStudentCourses(
                ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());
        List<StudentAssessment> assessmentList = RuleProcessorRuleUtils.getUniqueStudentAssessments(
                ruleProcessorData.getStudentAssessments(), ruleProcessorData.isProjected());
        List<StudentAssessment> excludedAssessments = RuleProcessorRuleUtils.getExcludedStudentAssessments(
                ruleProcessorData.getStudentAssessments(), ruleProcessorData.isProjected());
        ruleProcessorData.setExcludedAssessments(excludedAssessments);

        List<GradProgramRule> gradProgramRulesMatch = ruleProcessorData.getGradProgramRules()
                .stream()
                .filter(gradProgramRule -> "M".compareTo(gradProgramRule.getRequirementType()) == 0
                        && "Y".compareTo(gradProgramRule.getIsActive()) == 0
                        && "A".compareTo(gradProgramRule.getRuleCategory()) == 0)
                .collect(Collectors.toList());

        List<AssessmentRequirement> assessmentRequirements = ruleProcessorData.getAssessmentRequirements();
        List<AssessmentRequirement> originalAssessmentRequirements = new ArrayList<>(assessmentRequirements);

        logger.debug(String.format("#### Match Program Rule size: %s",gradProgramRulesMatch.size()));

        List<StudentAssessment> finalAssessmentList = new ArrayList<>();
        List<GradProgramRule> finalProgramRulesList = new ArrayList<>();
        StudentAssessment tempSA;
        GradProgramRule tempPR;
        ObjectMapper objectMapper = new ObjectMapper();

        ListIterator<StudentAssessment> assessmentIterator = assessmentList.listIterator();

        while (assessmentIterator.hasNext()) {
        	StudentAssessment tempAssessment = assessmentIterator.next();

            logger.debug("Processing Assessment: Code=" + tempAssessment.getAssessmentCode());
            logger.debug("Assessment Requirements size: " + assessmentRequirements.size());

            List<AssessmentRequirement> tempAssessmentRequirement = assessmentRequirements.stream()
                    .filter(ar -> tempAssessment.getAssessmentCode().compareTo(ar.getAssessmentCode()) == 0)
                    .collect(Collectors.toList());

            logger.debug("Temp Assessment Requirement: " + tempAssessmentRequirement);

            GradProgramRule tempProgramRule = null;

            if (!tempAssessmentRequirement.isEmpty()) {
                for(AssessmentRequirement ar:tempAssessmentRequirement) {
                	if(tempProgramRule == null) {
                	tempProgramRule = gradProgramRulesMatch.stream()
                        .filter(pr -> pr.getRuleCode().compareTo(ar.getRuleCode()) == 0)
                        .findAny()
                        .orElse(null);
                	}
                }
            }
            logger.debug("Temp Program Rule: " + tempProgramRule);

            if (!tempAssessmentRequirement.isEmpty() && tempProgramRule != null) {

                GradProgramRule finalTempProgramRule = tempProgramRule;
                if (requirementsMet.stream()
                        .filter(rm -> rm.getRule().equals(finalTempProgramRule.getRuleCode()))
                        .findAny().orElse(null) == null) {
                    tempAssessment.setUsed(true);

                    if (tempAssessment.getGradReqMet().length() > 0) {

                    	tempAssessment.setGradReqMet(tempAssessment.getGradReqMet() + ", " + tempProgramRule.getRuleCode());
                    	tempAssessment.setGradReqMetDetail(tempAssessment.getGradReqMetDetail() + ", " + tempProgramRule.getRuleCode()
                                + " - " + tempProgramRule.getRequirementName());
                    } else {
                    	tempAssessment.setGradReqMet(tempProgramRule.getRuleCode());
                    	tempAssessment.setGradReqMetDetail(tempProgramRule.getRuleCode() + " - " + tempProgramRule.getRequirementName());
                    }

                    tempProgramRule.setPassed(true);
                    requirementsMet.add(new GradRequirement(tempProgramRule.getRuleCode(), tempProgramRule.getRequirementName()));
                } else {
                    logger.debug("!!! Program Rule met Already: " + tempProgramRule);
                }
            }

            tempSA = new StudentAssessment();
            tempPR = new GradProgramRule();
            try {
            	tempSA = objectMapper.readValue(objectMapper.writeValueAsString(tempAssessment), StudentAssessment.class);
                if (tempSA != null)
                    finalAssessmentList.add(tempSA);
                logger.debug("TempSC: " + tempSA);
                logger.debug("Final Assessment List size: : " + finalAssessmentList.size());
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
        List<GradProgramRule> unusedRules = null;
		if(gradProgramRulesMatch.size() != finalProgramRulesList.size()) {
    		unusedRules = RuleEngineApiUtils.getCloneProgramRule(gradProgramRulesMatch);
    		unusedRules.removeAll(finalProgramRulesList);
    		finalProgramRulesList.addAll(unusedRules);
    	}
		
		for(GradProgramRule pr:finalProgramRulesList) {
			if(!pr.isPassed() && pr.getRuleCode().compareTo("116")==0) {
				for(StudentCourse sc:courseList) {
					if(sc.getMetLitNumRequirement() != null && (sc.getMetLitNumRequirement().equalsIgnoreCase("NME10") ||
							sc.getMetLitNumRequirement().equalsIgnoreCase("NME") ||
							sc.getMetLitNumRequirement().equalsIgnoreCase("NMF10") ||
							sc.getMetLitNumRequirement().equalsIgnoreCase("NMF"))) {
						createAssessmentRecord(finalAssessmentList,sc.getMetLitNumRequirement(),ruleProcessorData.getAssessmentList(),pr,ruleProcessorData.getGradStudent().getPen(),requirementsMet);
					}
				}
			}
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
                .filter(gradProgramRule -> "M".compareTo(gradProgramRule.getRequirementType()) != 0)
                .collect(Collectors.toList()));

        logger.debug("Final Program rules list size 2: " + finalProgramRulesList.size());
        finalAssessmentList.addAll(ruleProcessorData.getExcludedAssessments());
        ruleProcessorData.setStudentAssessments(finalAssessmentList);
        ruleProcessorData.setGradProgramRules(finalProgramRulesList);
        ruleProcessorData.setAssessmentRequirements(originalAssessmentRequirements);

        List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMet();

        if (reqsMet == null)
            reqsMet = new ArrayList<>();

        reqsMet.addAll(requirementsMet);
        ruleProcessorData.setRequirementsMet(reqsMet);

        return ruleProcessorData;
    }

    private void createAssessmentRecord(List<StudentAssessment> finalAssessmentList, String aCode, List<Assessment> assmList, GradProgramRule pr,String pen, List<GradRequirement> requirementsMet) {
    	StudentAssessment sA = new StudentAssessment();
    	sA.setAssessmentCode(aCode);
    	sA.setPen(pen);
    	Assessment asmt = assmList.stream()
    			  .filter(amt -> aCode.equals(amt.getAssessmentCode()))
    			  .findAny()
    			  .orElse(null);
    	if(asmt != null) {
    		sA.setAssessmentName(asmt.getAssessmentName());
    	}
    	sA.setGradReqMet(pr.getRuleCode());
        sA.setGradReqMetDetail(pr.getRuleCode() + " - " + pr.getRequirementName());
        sA.setSpecialCase("M");
        finalAssessmentList.add(sA);
        pr.setPassed(true);
        requirementsMet.add(new GradRequirement(pr.getRuleCode(), pr.getRequirementName()));
        	
	}

	@Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("AssessmentsMatchCreditsRule: Rule Processor Data set.");
    }

}
