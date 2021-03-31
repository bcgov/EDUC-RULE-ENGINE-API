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
public class FrenchImmersionMinElectiveCreditsRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(FrenchImmersionMinElectiveCreditsRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;
    final RuleType ruleType = RuleType.SPECIAL_MIN_CREDITS_ELECTIVE;

    public RuleProcessorData fire() {
    	    	
    	if(!ruleProcessorData.isHasSpecialProgramFrenchImmersion()) {
    		return ruleProcessorData;
    	}
    	List<GradRequirement> requirementsMet = new ArrayList<GradRequirement>();
        List<GradRequirement> requirementsNotMet = new ArrayList<GradRequirement>();

        List<StudentCourse> courseList = ruleProcessorData.getStudentCoursesForSpecialProgram();
        
        List<GradSpecialProgramRule> gradSpecialProgramMinCreditElectiveRulesMatch = ruleProcessorData.getGradSpecialProgramRulesFrenchImmersion()
                .stream()
                .filter(gradSpecialProgramRule -> "MCE".compareTo(gradSpecialProgramRule.getRequirementType()) == 0)
                .collect(Collectors.toList());
       
        logger.debug("#### Min Credit Elective Special Program Rule size: " + gradSpecialProgramMinCreditElectiveRulesMatch.size());

        List<StudentCourse> modifiedList = courseList.stream()
                .filter(sc -> !sc.isUsed())
                .collect(Collectors.toList());
        ListIterator<StudentCourse> studentCourseIterator = modifiedList.listIterator();
        int totalCredits = 0;        
        int requiredCredits = 0;        
        while (studentCourseIterator.hasNext()) {
            
        	StudentCourse sc = studentCourseIterator.next();
        	for(GradSpecialProgramRule pR:gradSpecialProgramMinCreditElectiveRulesMatch) {            	
            	if((pR.getRequiredLevel() == null || pR.getRequiredLevel().trim().compareTo("") == 0)) {
            		if(sc.getLanguage() != null && sc.getLanguage().equalsIgnoreCase("F")) {
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
	            		
	            		if (totalCredits == requiredCredits) {
	            			requirementsMet.add(new GradRequirement(pR.getRuleCode(), pR.getRequirementName()));
	            			pR.setPassed(true);
	            		}
	            		sc.setUsed(true);
            		}
            	}
            }
	       
            if ((totalCredits == requiredCredits) && totalCredits != 0) {
                break;
            }
        }
        
        List<StudentCourse> modifiedList2 = modifiedList.stream()
                .filter(sc -> sc.isUsed())
                .collect(Collectors.toList());
        ListIterator<StudentCourse> studentCourseIterator2 = modifiedList2.listIterator();
        int totalCreditsGrade11or12 = 0;
        int requiredCreditsGrad11or12 = 0;
        while (studentCourseIterator2.hasNext()) {            
        	StudentCourse sc = studentCourseIterator2.next();
            for(GradSpecialProgramRule pR:gradSpecialProgramMinCreditElectiveRulesMatch) {            	
            	if((pR.getRequiredLevel() != null && pR.getRequiredLevel().trim().compareTo("11 or 12") == 0)) {
            		if(sc.getLanguage() != null && sc.getLanguage().equalsIgnoreCase("F") && (sc.getCourseLevel().trim().equalsIgnoreCase("11") || sc.getCourseLevel().trim().equalsIgnoreCase("12"))) {
	            			requiredCreditsGrad11or12 = Integer.parseInt(pR.getRequiredCredits());
		            		if (totalCreditsGrade11or12 + sc.getCredits() <= requiredCreditsGrad11or12) {
		            			totalCreditsGrade11or12 += sc.getCredits();  
		    	                sc.setCreditsUsedForGrad(sc.getCredits());
		    	            }
		    	            else {
		    	                int extraCredits = totalCreditsGrade11or12 + sc.getCredits() - requiredCreditsGrad11or12;
		    	                totalCreditsGrade11or12 = requiredCreditsGrad11or12;
		    	                sc.setCreditsUsedForGrad(sc.getCredits() - extraCredits);
		    	            }
		            		
		            		if (totalCreditsGrade11or12 == requiredCreditsGrad11or12) {
		            			requirementsMet.add(new GradRequirement(pR.getRuleCode(), pR.getRequirementName()));
		            			pR.setPassed(true);
		            		}
		            	}
            		sc.setUsed(true);
            		}            		
            	}            
            if ((totalCreditsGrade11or12 == requiredCreditsGrad11or12) && totalCreditsGrade11or12 != 0) {
                break;
            }
        }            
        
        List<GradSpecialProgramRule> failedRules = gradSpecialProgramMinCreditElectiveRulesMatch.stream()
                .filter(pr -> !pr.isPassed()).collect(Collectors.toList());

        if (failedRules.isEmpty()) {
            logger.debug("All the Min Elective Credit rules met!");
        } else {
            for (GradSpecialProgramRule failedRule : failedRules) {
                requirementsNotMet.add(new GradRequirement(failedRule.getRuleCode(), failedRule.getNotMetDesc()));
            }
            logger.debug("One or more Min Elective Credit rules not met!");
        }
        
        ruleProcessorData.setRequirementsMet(requirementsMet);
        ruleProcessorData.setNonGradReasons(requirementsNotMet);

        
        return ruleProcessorData;
    }
    
    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("SpecialMinElectiveCreditRule: Rule Processor Data set.");
    }

}
