package ca.bc.gov.educ.api.ruleengine.rule;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.ruleengine.dto.GradRequirement;
import ca.bc.gov.educ.api.ruleengine.dto.ProgramRequirement;
import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class ProgramCompletionDateRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(ProgramCompletionDateRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {

    	List<ProgramRequirement> gradProgramRules = ruleProcessorData
				.getGradProgramRules().stream().filter(gpr -> "PCD".compareTo(gpr.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
						&& "Y".compareTo(gpr.getProgramRequirementCode().getActiveRequirement()) == 0)
				.collect(Collectors.toList());
    	for (ProgramRequirement gradProgramRule : gradProgramRules) {
	    	String programCompletionDate = ruleProcessorData.getGradStatus().getProgramCompletionDate();
	    	if(programCompletionDate != null) {
	    		 Date pCD = RuleEngineApiUtils.parsingTraxDate(programCompletionDate);
	    		 int diff = RuleEngineApiUtils.getDifferenceInDays(RuleProcessorRuleUtils.getProgramCompletionDate(pCD), RuleProcessorRuleUtils.getCurrentDate());
	    		 if(diff >= 0) {
	    			logger.debug("{} Passed",gradProgramRule.getProgramRequirementCode().getLabel());
	 				gradProgramRule.getProgramRequirementCode().setPassed(true);
	 				List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMet();
	 				if (reqsMet == null)
	 					reqsMet = new ArrayList<>();
	 				reqsMet.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getTraxReqNumber(), gradProgramRule.getProgramRequirementCode().getLabel()));
	 				ruleProcessorData.setRequirementsMet(reqsMet);
	 				return ruleProcessorData;
	    		 }
	    	}
    		logger.debug("{} Failed!",gradProgramRule.getProgramRequirementCode().getDescription());
			ruleProcessorData.setGraduated(false);
			List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasons();
			if (nonGradReasons == null)
				nonGradReasons = new ArrayList<>();
			nonGradReasons.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getTraxReqNumber(), gradProgramRule.getProgramRequirementCode().getNotMetDesc()));
			ruleProcessorData.setNonGradReasons(nonGradReasons);
	    	
    	}
        return ruleProcessorData;
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("ProgramCompletionDateRule: Rule Processor Data set.");
    }
}
