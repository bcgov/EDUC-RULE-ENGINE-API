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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class CareerProgramMatchRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(CareerProgramMatchRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    public RuleData fire() {
        Map<String,OptionalProgramRuleProcessor> mapOptional = ruleProcessorData.getMapOptional();
        OptionalProgramRuleProcessor obj = mapOptional.get("CP");
    	if(obj == null || !obj.isHasOptionalProgram()) {
    		return ruleProcessorData;
    	}
        obj.setOptionalProgramGraduated(true);
    	List<GradRequirement> requirementsMet = new ArrayList<>();
        List<GradRequirement> requirementsNotMet = new ArrayList<>();

        List<StudentCourse> courseList = RuleProcessorRuleUtils.getUniqueStudentCourses(
        		obj.getStudentCoursesOptionalProgram(), ruleProcessorData.isProjected());
        List<OptionalProgramRequirement> careerProgramRulesMatch = obj.getOptionalProgramRules()
                .stream()
                .filter(gradOptionalProgramRule -> "M".compareTo(gradOptionalProgramRule.getOptionalProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0 
                		&& "Y".compareTo(gradOptionalProgramRule.getOptionalProgramRequirementCode().getActiveRequirement()) == 0
                		&& "C".compareTo(gradOptionalProgramRule.getOptionalProgramRequirementCode().getRequirementCategory()) == 0)
                .toList();
       
        logger.debug("#### Career Program Rule size: {}", careerProgramRulesMatch.size());

        ListIterator<StudentCourse> studentCourseIterator = courseList.listIterator();
        int totalCredits = 0;        
        int requiredCredits = 0;     
        List<StudentCourse> finalCourseList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        while (studentCourseIterator.hasNext()) {
            
        	StudentCourse sc = studentCourseIterator.next();
        	for(OptionalProgramRequirement pR:careerProgramRulesMatch) {            	
            	if((pR.getOptionalProgramRequirementCode().getRequiredLevel() == null || pR.getOptionalProgramRequirementCode().getRequiredLevel().trim().compareTo("") == 0) && (sc.getWorkExpFlag() != null && sc.getWorkExpFlag().equalsIgnoreCase("Y"))) {
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
                        requirementsMet.add(new GradRequirement(pR.getOptionalProgramRequirementCode().getOptProReqCode(), pR.getOptionalProgramRequirementCode().getLabel(),pR.getOptionalProgramRequirementCode().getOptProReqCode()));
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
            AlgorithmSupportRule.copyAndAddIntoStudentCoursesList(sc, finalCourseList, objectMapper);
	       
            if ((totalCredits >= requiredCredits) && totalCredits != 0) {
                break;
            }
        }
        
        obj.setStudentCoursesOptionalProgram(RuleEngineApiUtils.getClone(finalCourseList));
        List<GradRequirement> resMet = obj.getRequirementsMetOptionalProgram();

        if (resMet == null)
            resMet = new ArrayList<>();

        resMet.addAll(requirementsMet);

        obj.setRequirementsMetOptionalProgram(resMet);
        
        List<OptionalProgramRequirement> failedRules = careerProgramRulesMatch.stream()
                .filter(pr -> !pr.getOptionalProgramRequirementCode().isPassed()).toList();

        if (failedRules.isEmpty()) {
            logger.debug("All the Career Program Match rules met!");
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
            logger.debug("One or more Career Program rules not met!");
        }
        mapOptional.put("CP",obj);
        ruleProcessorData.setMapOptional(mapOptional);
        return ruleProcessorData;
    }
    
    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
    }

}
