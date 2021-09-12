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

import ca.bc.gov.educ.api.ruleengine.dto.GradRequirement;
import ca.bc.gov.educ.api.ruleengine.dto.OptionalProgramRequirement;
import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class CareerProgramMatchRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(CareerProgramMatchRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    public RuleData fire() {

    	if(!ruleProcessorData.isHasSpecialProgramCareerProgram()) {
    		return ruleProcessorData;
    	}
    	ruleProcessorData.setSpecialProgramCareerProgramGraduated(true);
    	List<GradRequirement> requirementsMet = new ArrayList<>();
        List<GradRequirement> requirementsNotMet = new ArrayList<>();

        List<StudentCourse> courseList = RuleProcessorRuleUtils.getUniqueStudentCourses(
        		ruleProcessorData.getStudentCoursesForCareerProgram(), ruleProcessorData.isProjected());
        List<OptionalProgramRequirement> careerProgramRulesMatch = ruleProcessorData.getGradSpecialProgramRulesCareerProgram()
                .stream()
                .filter(gradSpecialProgramRule -> "M".compareTo(gradSpecialProgramRule.getOptionalProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0 
                		&& "Y".compareTo(gradSpecialProgramRule.getOptionalProgramRequirementCode().getActiveRequirement()) == 0
                		&& "C".compareTo(gradSpecialProgramRule.getOptionalProgramRequirementCode().getRequirementCategory()) == 0)
                .collect(Collectors.toList());
       
        logger.debug("#### Career Program Rule size: " + careerProgramRulesMatch.size());

        ListIterator<StudentCourse> studentCourseIterator = courseList.listIterator();
        int totalCredits = 0;        
        int requiredCredits = 0;     
        List<StudentCourse> finalCourseList = new ArrayList<StudentCourse>();
        ObjectMapper objectMapper = new ObjectMapper();
        while (studentCourseIterator.hasNext()) {
            
        	StudentCourse sc = studentCourseIterator.next();
        	for(OptionalProgramRequirement pR:careerProgramRulesMatch) {            	
            	if((pR.getOptionalProgramRequirementCode().getRequiredLevel() == null || pR.getOptionalProgramRequirementCode().getRequiredLevel().trim().compareTo("") == 0)) {
            		if(sc.getWorkExpFlag() != null && sc.getWorkExpFlag().equalsIgnoreCase("Y")) {
	            		requiredCredits = Integer.parseInt(pR.getOptionalProgramRequirementCode().getRequiredCredits());
	            		if (totalCredits + sc.getCredits() <= requiredCredits) {
	    	                totalCredits += sc.getCredits();  
	    	                sc.setCreditsUsedForGrad(sc.getCredits());
	    	            }
	    	            else {
	    	                int extraCredits = totalCredits + sc.getCredits() - requiredCredits;
	    	                totalCredits = requiredCredits;
	    	                sc.setCreditsUsedForGrad(sc.getCredits() - extraCredits);
	    	            }
	            		
	            		if (totalCredits >= requiredCredits) {
	            			requirementsMet.add(new GradRequirement(pR.getOptionalProgramRequirementCode().getOptProReqCode(), pR.getOptionalProgramRequirementCode().getLabel()));
	            			pR.getOptionalProgramRequirementCode().setPassed(true);
	            		}
	            		if (sc.getGradReqMet().length() > 0) {

	    					sc.setGradReqMet(sc.getGradReqMet() + ", " + pR.getOptionalProgramRequirementCode().getOptProReqCode());
	    					sc.setGradReqMetDetail(sc.getGradReqMetDetail() + ", " + pR.getOptionalProgramRequirementCode().getOptProReqCode() + " - "
	    							+ pR.getOptionalProgramRequirementCode().getLabel());
	    				} else {
	    					sc.setGradReqMet(pR.getOptionalProgramRequirementCode().getOptProReqCode());
	    					sc.setGradReqMetDetail(
	    							pR.getOptionalProgramRequirementCode().getOptProReqCode() + " - " + pR.getOptionalProgramRequirementCode().getLabel());
	    				}
	            		sc.setUsed(true);
            		}
            	}
            }
        	StudentCourse tempSC = new StudentCourse();
            try {
                tempSC = objectMapper.readValue(objectMapper.writeValueAsString(sc), StudentCourse.class);
                if (tempSC != null)
                    finalCourseList.add(tempSC);
            } catch (IOException e) {
                logger.error("ERROR:" + e.getMessage());
            }
	       
            if ((totalCredits >= requiredCredits) && totalCredits != 0) {
                break;
            }
        }
        
        ruleProcessorData.setStudentCoursesForCareerProgram(RuleEngineApiUtils.getClone(finalCourseList));
        List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMetSpecialProgramsCareerProgram();

        if (reqsMet == null)
            reqsMet = new ArrayList<>();

        reqsMet.addAll(requirementsMet);

        ruleProcessorData.setRequirementsMetSpecialProgramsCareerProgram(reqsMet);        
        
        List<OptionalProgramRequirement> failedRules = careerProgramRulesMatch.stream()
                .filter(pr -> !pr.getOptionalProgramRequirementCode().isPassed()).collect(Collectors.toList());

        if (failedRules.isEmpty()) {
            logger.debug("All the Career Program Match rules met!");
        } else {
            for (OptionalProgramRequirement failedRule : failedRules) {
                requirementsNotMet.add(new GradRequirement(failedRule.getOptionalProgramRequirementCode().getOptProReqCode(), failedRule.getOptionalProgramRequirementCode().getNotMetDesc()));
            }
            List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasonsSpecialProgramsCareerProgram();

            if (nonGradReasons == null)
                nonGradReasons = new ArrayList<>();

            nonGradReasons.addAll(requirementsNotMet);
            ruleProcessorData.setNonGradReasonsSpecialProgramsCareerProgram(nonGradReasons);
            ruleProcessorData.setSpecialProgramCareerProgramGraduated(false);
            logger.debug("One or more Career Program rules not met!");
        }        
        return ruleProcessorData;
    }
    
    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("CareerProgramMatchRule: Rule Processor Data set.");
    }

}
