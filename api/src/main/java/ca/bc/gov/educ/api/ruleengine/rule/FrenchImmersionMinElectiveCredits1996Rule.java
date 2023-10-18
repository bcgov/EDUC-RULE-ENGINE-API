package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class FrenchImmersionMinElectiveCredits1996Rule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(FrenchImmersionMinElectiveCredits1996Rule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    public RuleProcessorData fire() {

        Map<String,OptionalProgramRuleProcessor> mapOptional = ruleProcessorData.getMapOptional();
        OptionalProgramRuleProcessor obj = mapOptional.get("FI");

    	if(obj == null || !obj.isHasOptionalProgram()) {
    		return ruleProcessorData;
    	}
    	List<StudentCourse> courseList = obj.getStudentCoursesOptionalProgram();
        List<OptionalProgramRequirement> gradOptionalProgramMinCreditElectiveRulesMatch = obj.getOptionalProgramRules()
                .stream()
                .filter(gradOptionalProgramRule -> "MCE".compareTo(gradOptionalProgramRule.getOptionalProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
                		&& "Y".compareTo(gradOptionalProgramRule.getOptionalProgramRequirementCode().getActiveRequirement()) == 0
                		&& "C".compareTo(gradOptionalProgramRule.getOptionalProgramRequirementCode().getRequirementCategory()) == 0)
                .toList();
       
        logger.debug("#### French Immersion Min Credit Elective Optional Program Rule size: {}",gradOptionalProgramMinCreditElectiveRulesMatch.size());
        ObjectMapper objectMapper = new ObjectMapper();

        List<StudentCourse> modifiedList = RuleEngineApiUtils.getClone(courseList.stream().filter(sc -> !sc.isUsed()).sorted(Comparator.comparing(StudentCourse::getCourseLevel).reversed()).toList());

        List<GradRequirement> requirementsMet = new ArrayList<>();
        List<GradRequirement> requirementsNotMet = new ArrayList<>();
        List<StudentCourse> finalCourseList2 = new ArrayList<>();
        ListIterator<StudentCourse> studentCourseIterator2 = modifiedList.listIterator();
        int totalCreditsGrade11or12 = 0;
        int requiredCreditsGrad11or12 = 0;
        while (studentCourseIterator2.hasNext()) {            
        	StudentCourse sc = studentCourseIterator2.next();
            for(OptionalProgramRequirement pR:gradOptionalProgramMinCreditElectiveRulesMatch) {
                if(pR.getOptionalProgramRequirementCode().getRequiredLevel() != null && pR.getOptionalProgramRequirementCode().getRequiredLevel().trim().compareTo("11 or 12") == 0 && sc.getLanguage() != null && sc.getLanguage().equalsIgnoreCase("F") && (sc.getCourseLevel().trim().contains("11") || sc.getCourseLevel().trim().contains("12"))) {
                    requiredCreditsGrad11or12 = Integer.parseInt(pR.getOptionalProgramRequirementCode().getRequiredCredits());
                    totalCreditsGrade11or12 = processCredits(pR,totalCreditsGrade11or12,sc,requirementsMet);
                }
            }
            AlgorithmSupportRule.copyAndAddIntoStudentCoursesList(sc, finalCourseList2, objectMapper);
            
            if ((totalCreditsGrade11or12 == requiredCreditsGrad11or12) && totalCreditsGrade11or12 != 0) {
            	break;
            }            
        }            
        List<GradRequirement> resMet = obj.getRequirementsMetOptionalProgram();

        if (resMet == null)
            resMet = new ArrayList<>();

        resMet.addAll(requirementsMet);

        obj.setRequirementsMetOptionalProgram(resMet);
        finalCourseList2.addAll(courseList.stream().filter(StudentCourse::isUsed).toList());
        obj.setStudentCoursesOptionalProgram(RuleEngineApiUtils.getClone(finalCourseList2));
        List<OptionalProgramRequirement> failedRules = gradOptionalProgramMinCreditElectiveRulesMatch.stream()
                .filter(pr -> !pr.getOptionalProgramRequirementCode().isPassed()).toList();

        if (failedRules.isEmpty()) {
            logger.debug("All the Min Elective Credit rules met!");
        } else {
            for (OptionalProgramRequirement failedRule : failedRules) {
                requirementsNotMet.add(new GradRequirement(failedRule.getOptionalProgramRequirementCode().getOptProReqCode(), failedRule.getOptionalProgramRequirementCode().getNotMetDesc(),failedRule.getOptionalProgramRequirementCode().getOptProReqCode()));
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
			requirementsMet.add(new GradRequirement(pR.getOptionalProgramRequirementCode().getOptProReqCode(), pR.getOptionalProgramRequirementCode().getLabel(),pR.getOptionalProgramRequirementCode().getOptProReqCode()));
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
    }

}
