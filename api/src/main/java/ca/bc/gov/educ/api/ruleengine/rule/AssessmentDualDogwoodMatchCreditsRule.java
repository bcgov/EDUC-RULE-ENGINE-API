package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.OptionalProgramRuleProcessor;
import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class AssessmentDualDogwoodMatchCreditsRule implements Rule {

	private static Logger logger = LoggerFactory.getLogger(AssessmentDualDogwoodMatchCreditsRule.class);

	@Override
	public RuleData fire(RuleProcessorData ruleProcessorData) {
		Map<String, OptionalProgramRuleProcessor> mapOptional = ruleProcessorData.getMapOptional();
		OptionalProgramRuleProcessor obj = mapOptional.get("DD");
		if (obj == null || !obj.isHasOptionalProgram()) {
			return ruleProcessorData;
		}
		OptionalProgramMatchRule.processOptionalProgramAssessmentMatchRule(obj,ruleProcessorData);
		mapOptional.put("DD",obj);
		ruleProcessorData.setMapOptional(mapOptional);
		return ruleProcessorData;
	}

}
