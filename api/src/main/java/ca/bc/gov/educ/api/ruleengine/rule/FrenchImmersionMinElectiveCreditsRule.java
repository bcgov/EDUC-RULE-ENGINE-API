package ca.bc.gov.educ.api.ruleengine.rule;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.educ.api.ruleengine.dto.GradRequirement;
import ca.bc.gov.educ.api.ruleengine.dto.OptionalProgramRequirement;
import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class FrenchImmersionMinElectiveCreditsRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(FrenchImmersionMinElectiveCreditsRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    public RuleProcessorData fire() {
    	    	
    	if(!ruleProcessorData.isHasSpecialProgramFrenchImmersion()) {
    		return ruleProcessorData;
    	}
    	List<GradRequirement> requirementsMet = new ArrayList<>();
        
        List<StudentCourse> courseList = ruleProcessorData.getStudentCoursesForFrenchImmersion();
        List<OptionalProgramRequirement> gradSpecialProgramMinCreditElectiveRulesMatch = ruleProcessorData.getGradSpecialProgramRulesFrenchImmersion()
                .stream()
                .filter(gradSpecialProgramRule -> "MCE".compareTo(gradSpecialProgramRule.getOptionalProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
                		&& "Y".compareTo(gradSpecialProgramRule.getOptionalProgramRequirementCode().getActiveRequirement()) == 0
                		&& "C".compareTo(gradSpecialProgramRule.getOptionalProgramRequirementCode().getRequirementCategory()) == 0)
                .collect(Collectors.toList());
       
        logger.debug("#### French Immersion Min Credit Elective Special Program Rule size: " + gradSpecialProgramMinCreditElectiveRulesMatch.size());
        List<StudentCourse> finalCourseList = new ArrayList<>();
        List<StudentCourse> finalCourseList2 = new ArrayList<>();
        StudentCourse tempSC;
        ObjectMapper objectMapper = new ObjectMapper();
        List<StudentCourse> matchedList = courseList
        		.stream()
        		.filter(sc -> (sc.isUsed()))
        		.collect(Collectors.toList());
        
        List<StudentCourse> modifiedList = courseList
        		.stream()
                .filter(sc -> !sc.isUsed())
                .collect(Collectors.toList());
        Collections.sort(modifiedList, Comparator.comparing(StudentCourse::getCourseLevel).reversed());
        ListIterator<StudentCourse> studentCourseIterator = modifiedList.listIterator();
        int totalCredits = 0;        
        int requiredCredits = 0;
        boolean requirementAchieved = false;
        while (studentCourseIterator.hasNext()) {
            
        	StudentCourse sc = studentCourseIterator.next();
        	if(!requirementAchieved) {
	        	for(OptionalProgramRequirement pR:gradSpecialProgramMinCreditElectiveRulesMatch) {            	
	        		if((pR.getOptionalProgramRequirementCode().getRequiredLevel() == null || pR.getOptionalProgramRequirementCode().getRequiredLevel().trim().compareTo("") == 0) && sc.getLanguage() != null && sc.getLanguage().equalsIgnoreCase("F")) {
	        			requiredCredits = Integer.parseInt(pR.getOptionalProgramRequirementCode().getRequiredCredits());
	        			totalCredits = processCredits(pR,totalCredits,sc,requirementsMet);
	        		}
	            }
        	}
        	tempSC = new StudentCourse();
            try {
                tempSC = objectMapper.readValue(objectMapper.writeValueAsString(sc), StudentCourse.class);
                if (tempSC != null)
                    finalCourseList.add(tempSC);
            } catch (IOException e) {
                logger.error("ERROR:" + e.getMessage());
            }
            if ((totalCredits == requiredCredits) && totalCredits != 0) {
            	requirementAchieved = true;
            }
        }
        List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMetSpecialProgramsFrenchImmersion();

        if (reqsMet == null)
            reqsMet = new ArrayList<>();

        reqsMet.addAll(requirementsMet);

        ruleProcessorData.setRequirementsMetSpecialProgramsFrenchImmersion(reqsMet);
        requirementsMet = new ArrayList<>();
        List<GradRequirement> requirementsNotMet = new ArrayList<>();
        ListIterator<StudentCourse> studentCourseIterator2 = finalCourseList.listIterator();
        int totalCreditsGrade11or12 = 0;
        int requiredCreditsGrad11or12 = 0;
        requirementAchieved = false;
        while (studentCourseIterator2.hasNext()) {            
        	StudentCourse sc = studentCourseIterator2.next();
        	if(!requirementAchieved && sc.isUsed()) {
        		for(OptionalProgramRequirement pR:gradSpecialProgramMinCreditElectiveRulesMatch) {            	
        			if(pR.getOptionalProgramRequirementCode().getRequiredLevel() != null && pR.getOptionalProgramRequirementCode().getRequiredLevel().trim().compareTo("11 or 12") == 0 && sc.getLanguage() != null && sc.getLanguage().equalsIgnoreCase("F") && (sc.getCourseLevel().trim().equalsIgnoreCase("11") || sc.getCourseLevel().trim().equalsIgnoreCase("12"))) {
        				requiredCreditsGrad11or12 = Integer.parseInt(pR.getOptionalProgramRequirementCode().getRequiredCredits());
        				totalCreditsGrade11or12 = processCredits(pR,totalCreditsGrade11or12,sc,requirementsMet);
        			}         		
        		}
        	}
        	
        	tempSC = new StudentCourse();
            try {
                tempSC = objectMapper.readValue(objectMapper.writeValueAsString(sc), StudentCourse.class);
                if (tempSC != null)
                	finalCourseList2.add(tempSC);
            } catch (IOException e) {
                logger.error("ERROR:" + e.getMessage());
            }
            
            if ((totalCreditsGrade11or12 == requiredCreditsGrad11or12) && totalCreditsGrade11or12 != 0) {
            	requirementAchieved = true;
            }            
        }            
        reqsMet = ruleProcessorData.getRequirementsMetSpecialProgramsFrenchImmersion();

        if (reqsMet == null)
            reqsMet = new ArrayList<>();

        reqsMet.addAll(requirementsMet);

        ruleProcessorData.setRequirementsMetSpecialProgramsFrenchImmersion(reqsMet);
        finalCourseList2.addAll(matchedList);
        ruleProcessorData.setStudentCoursesForFrenchImmersion(RuleEngineApiUtils.getClone(finalCourseList2));
        List<OptionalProgramRequirement> failedRules = gradSpecialProgramMinCreditElectiveRulesMatch.stream()
                .filter(pr -> !pr.getOptionalProgramRequirementCode().isPassed()).collect(Collectors.toList());

        if (failedRules.isEmpty()) {
            logger.debug("All the Min Elective Credit rules met!");
        } else {
            for (OptionalProgramRequirement failedRule : failedRules) {
                requirementsNotMet.add(new GradRequirement(failedRule.getOptionalProgramRequirementCode().getOptProReqCode(), failedRule.getOptionalProgramRequirementCode().getNotMetDesc()));
            }
            List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasonsSpecialProgramsFrenchImmersion();

            if (nonGradReasons == null)
                nonGradReasons = new ArrayList<>();

            nonGradReasons.addAll(requirementsNotMet);
            ruleProcessorData.setNonGradReasonsSpecialProgramsFrenchImmersion(nonGradReasons);
            ruleProcessorData.setSpecialProgramFrenchImmersionGraduated(false);
            logger.debug("One or more Min Elective Credit rules not met!");
        }        
        return ruleProcessorData;
    }
    
    public int processCredits(OptionalProgramRequirement pR, int totalCredits, StudentCourse sc, List<GradRequirement> requirementsMet) {
    	
		int requiredCredits = Integer.parseInt(pR.getOptionalProgramRequirementCode().getRequiredCredits());
		if (totalCredits + sc.getCredits() <= requiredCredits) {
			totalCredits += sc.getCredits();  
            sc.setCreditsUsedForGrad(sc.getCredits());
        }
        else {
            int extraCredits = totalCredits + sc.getCredits() - requiredCredits;
            totalCredits = requiredCredits;
            sc.setCreditsUsedForGrad(sc.getCredits() - extraCredits);
        }
		
		if (totalCredits == requiredCredits) {
			requirementsMet.add(new GradRequirement(pR.getOptionalProgramRequirementCode().getOptProReqCode(), pR.getOptionalProgramRequirementCode().getLabel()));
			pR.getOptionalProgramRequirementCode().setPassed(true);
		}
		sc.setUsed(true);
		if (sc.getGradReqMet().length() > 0) {

            sc.setGradReqMet(sc.getGradReqMet() + ", " + pR.getOptionalProgramRequirementCode().getOptProReqCode());
            sc.setGradReqMetDetail(sc.getGradReqMetDetail() + ", " + pR.getOptionalProgramRequirementCode().getOptProReqCode()
                    + " - " + pR.getOptionalProgramRequirementCode().getLabel());
        } else {
            sc.setGradReqMet(pR.getOptionalProgramRequirementCode().getOptProReqCode());
            sc.setGradReqMetDetail(pR.getOptionalProgramRequirementCode().getOptProReqCode() + " - " + pR.getOptionalProgramRequirementCode().getLabel());
        }	            		
    	
    	return totalCredits;
    }
    
    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("FrenchImmersionMinElectiveCreditRule: Rule Processor Data set.");
    }

}
