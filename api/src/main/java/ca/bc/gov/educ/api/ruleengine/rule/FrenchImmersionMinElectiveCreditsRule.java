package ca.bc.gov.educ.api.ruleengine.rule;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

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

        Map<String,OptionalProgramRuleProcessor> mapOptional = ruleProcessorData.getMapOptional();
        OptionalProgramRuleProcessor obj = mapOptional.get("FI");

    	if(obj == null || !obj.isHasOptionalProgram()) {
    		return ruleProcessorData;
    	}
    	List<GradRequirement> requirementsMet = new ArrayList<>();
        
        List<StudentCourse> courseList = obj.getStudentCoursesOptionalProgram();
        List<OptionalProgramRequirement> gradOptionalProgramMinCreditElectiveRulesMatch = obj.getOptionalProgramRules()
                .stream()
                .filter(gradOptionalProgramRule -> "MCE".compareTo(gradOptionalProgramRule.getOptionalProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
                		&& "Y".compareTo(gradOptionalProgramRule.getOptionalProgramRequirementCode().getActiveRequirement()) == 0
                		&& "C".compareTo(gradOptionalProgramRule.getOptionalProgramRequirementCode().getRequirementCategory()) == 0)
                .collect(Collectors.toList());
       
        logger.debug("#### French Immersion Min Credit Elective Optional Program Rule size: " + gradOptionalProgramMinCreditElectiveRulesMatch.size());
        List<StudentCourse> finalCourseList = new ArrayList<>();
        List<StudentCourse> finalCourseList2 = new ArrayList<>();
        StudentCourse tempSC;
        ObjectMapper objectMapper = new ObjectMapper();
        List<StudentCourse> matchedList = courseList
        		.stream()
        		.filter(StudentCourse::isUsed)
        		.collect(Collectors.toList());

        List<StudentCourse> modifiedList = courseList.stream().filter(sc -> !sc.isUsed()).sorted(Comparator.comparing(StudentCourse::getCourseLevel).reversed()).collect(Collectors.toList());
        ListIterator<StudentCourse> studentCourseIterator = modifiedList.listIterator();
        int totalCredits = 0;        
        int requiredCredits = 0;
        boolean requirementAchieved = false;
        while (studentCourseIterator.hasNext()) {
            
        	StudentCourse sc = studentCourseIterator.next();
        	if(!requirementAchieved) {
	        	for(OptionalProgramRequirement pR:gradOptionalProgramMinCreditElectiveRulesMatch) {            	
	        		if((pR.getOptionalProgramRequirementCode().getRequiredLevel() == null || pR.getOptionalProgramRequirementCode().getRequiredLevel().trim().compareTo("") == 0) && sc.getLanguage() != null && sc.getLanguage().equalsIgnoreCase("F")) {
	        			requiredCredits = Integer.parseInt(pR.getOptionalProgramRequirementCode().getRequiredCredits());
	        			totalCredits = processCredits(pR,totalCredits,sc,requirementsMet);
	        		}
	            }
        	}
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
        List<GradRequirement> resMet = obj.getRequirementsMetOptionalProgram();

        if (resMet == null)
            resMet = new ArrayList<>();

        resMet.addAll(requirementsMet);

        obj.setRequirementsMetOptionalProgram(resMet);
        requirementsMet = new ArrayList<>();
        List<GradRequirement> requirementsNotMet = new ArrayList<>();
        ListIterator<StudentCourse> studentCourseIterator2 = finalCourseList.listIterator();
        int totalCreditsGrade11or12 = 0;
        int requiredCreditsGrad11or12 = 0;
        requirementAchieved = false;
        while (studentCourseIterator2.hasNext()) {            
        	StudentCourse sc = studentCourseIterator2.next();
        	if(!requirementAchieved && sc.isUsed()) {
        		for(OptionalProgramRequirement pR:gradOptionalProgramMinCreditElectiveRulesMatch) {            	
        			if(pR.getOptionalProgramRequirementCode().getRequiredLevel() != null && pR.getOptionalProgramRequirementCode().getRequiredLevel().trim().compareTo("11 or 12") == 0 && sc.getLanguage() != null && sc.getLanguage().equalsIgnoreCase("F") && (sc.getCourseLevel().trim().equalsIgnoreCase("11") || sc.getCourseLevel().trim().equalsIgnoreCase("12"))) {
        				requiredCreditsGrad11or12 = Integer.parseInt(pR.getOptionalProgramRequirementCode().getRequiredCredits());
        				totalCreditsGrade11or12 = processCredits(pR,totalCreditsGrade11or12,sc,requirementsMet);
        			}         		
        		}
        	}

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
        resMet = obj.getRequirementsMetOptionalProgram();

        if (resMet == null)
            resMet = new ArrayList<>();

        resMet.addAll(requirementsMet);

        obj.setRequirementsMetOptionalProgram(resMet);
        finalCourseList2.addAll(matchedList);
        obj.setStudentCoursesOptionalProgram(RuleEngineApiUtils.getClone(finalCourseList2));
        List<OptionalProgramRequirement> failedRules = gradOptionalProgramMinCreditElectiveRulesMatch.stream()
                .filter(pr -> !pr.getOptionalProgramRequirementCode().isPassed()).collect(Collectors.toList());

        if (failedRules.isEmpty()) {
            logger.debug("All the Min Elective Credit rules met!");
        } else {
            for (OptionalProgramRequirement failedRule : failedRules) {
                requirementsNotMet.add(new GradRequirement(failedRule.getOptionalProgramRequirementCode().getOptProReqCode(), failedRule.getOptionalProgramRequirementCode().getNotMetDesc()));
            }
            List<GradRequirement> nonGradReasons = obj.getNonGradReasonsOptionalProgram();

            if (nonGradReasons == null)
                nonGradReasons = new ArrayList<>();

            nonGradReasons.addAll(requirementsNotMet);
            obj.setNonGradReasonsOptionalProgram(nonGradReasons);
            obj.setOptionalProgramGraduated(false);
            logger.debug("One or more Min Elective Credit rules not met!");
        }
        mapOptional.put("FI",obj);
        ruleProcessorData.setMapOptional(mapOptional);
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
