package ca.bc.gov.educ.api.ruleengine.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
public class CareerProgramMatchRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(CareerProgramMatchRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;
    
    final RuleType ruleType = RuleType.MATCH;

    public RuleData fire() {

    	if(!ruleProcessorData.isHasSpecialProgramCareerProgram()) {
    		return ruleProcessorData;
    	}
    	ruleProcessorData.setSpecialProgramCareerProgramGraduated(true);
    	List<GradRequirement> requirementsMet = new ArrayList<GradRequirement>();
        List<GradRequirement> requirementsNotMet = new ArrayList<GradRequirement>();

        List<StudentCourse> courseList = ruleProcessorData.getStudentCoursesForCareerProgram();
        
        List<GradSpecialProgramRule> careerProgramRulesMatch = ruleProcessorData.getGradSpecialProgramRulesCareerProgram()
                .stream()
                .filter(gradSpecialProgramRule -> "M".compareTo(gradSpecialProgramRule.getRequirementType()) == 0)
                .collect(Collectors.toList());
       
        logger.debug("#### Career Program Rule size: " + careerProgramRulesMatch.size());

        ListIterator<StudentCourse> studentCourseIterator = courseList.listIterator();
        int totalCredits = 0;        
        int requiredCredits = 0;        
        while (studentCourseIterator.hasNext()) {
            
        	StudentCourse sc = studentCourseIterator.next();
        	for(GradSpecialProgramRule pR:careerProgramRulesMatch) {            	
            	if((pR.getRequiredLevel() == null || pR.getRequiredLevel().trim().compareTo("") == 0)) {
            		if(sc.getWorkExpFlag() != null && sc.getWorkExpFlag().equalsIgnoreCase("Y")) {
	            		requiredCredits = Integer.parseInt(pR.getRequiredCredits());
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
	            			requirementsMet.add(new GradRequirement(pR.getRuleCode(), pR.getRequirementName()));
	            			pR.setPassed(true);
	            		}
	            		sc.setUsed(true);
            		}
            	}
            }
	       
            if ((totalCredits >= requiredCredits) && totalCredits != 0) {
                break;
            }
        }
        List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMetSpecialPrograms();

        if (reqsMet == null)
            reqsMet = new ArrayList<GradRequirement>();

        reqsMet.addAll(requirementsMet);

        ruleProcessorData.setRequirementsMetSpecialPrograms(reqsMet);        
        
        List<GradSpecialProgramRule> failedRules = careerProgramRulesMatch.stream()
                .filter(pr -> !pr.isPassed()).collect(Collectors.toList());

        if (failedRules.isEmpty()) {
            logger.debug("All the Career Program Match rules met!");
        } else {
            for (GradSpecialProgramRule failedRule : failedRules) {
                requirementsNotMet.add(new GradRequirement(failedRule.getRuleCode(), failedRule.getNotMetDesc()));
            }
            List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasonsSpecialPrograms();

            if (nonGradReasons == null)
                nonGradReasons = new ArrayList<GradRequirement>();

            nonGradReasons.addAll(requirementsNotMet);
            ruleProcessorData.setNonGradReasonsSpecialPrograms(nonGradReasons);
            ruleProcessorData.setSpecialProgramCareerProgramGraduated(false);
            logger.debug("One or more Career Program rules not met!");
        }        
        return ruleProcessorData;
    }


    public boolean fire(Object inputData, Object outputData) {
        return false;
    }
    
    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("CareerProgramMatchRule: Rule Processor Data set.");
    }

}
