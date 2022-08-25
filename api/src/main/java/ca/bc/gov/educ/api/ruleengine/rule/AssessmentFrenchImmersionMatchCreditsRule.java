package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.OptionalProgramRuleProcessor;
import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class AssessmentFrenchImmersionMatchCreditsRule implements Rule {

	private static Logger logger = LoggerFactory.getLogger(AssessmentFrenchImmersionMatchCreditsRule.class);

	@Override
	public RuleData fire(RuleProcessorData ruleProcessorData) {
		Map<String, OptionalProgramRuleProcessor> mapOptional = ruleProcessorData.getMapOptional();
		OptionalProgramRuleProcessor obj = mapOptional.get("FI");
		if (obj == null || !obj.isHasOptionalProgram()) {
			return ruleProcessorData;
		}
		OptionalProgramMatchRule.processOptionalProgramAssessmentMatchRule(obj,ruleProcessorData);
		mapOptional.put("FI",obj);
		ruleProcessorData.setMapOptional(mapOptional);
		return ruleProcessorData;
	}

}
