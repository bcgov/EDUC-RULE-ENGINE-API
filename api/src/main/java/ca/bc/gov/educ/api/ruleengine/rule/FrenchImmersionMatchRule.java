package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.OptionalProgramRuleProcessor;
import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Data
@AllArgsConstructor
public class FrenchImmersionMatchRule implements Rule {

	private static Logger logger = LoggerFactory.getLogger(FrenchImmersionMatchRule.class);

	private RuleProcessorData ruleProcessorData;


	public RuleData fire() {
		Map<String, OptionalProgramRuleProcessor> mapOptional = ruleProcessorData.getMapOptional();
		OptionalProgramRuleProcessor obj = mapOptional.get("FI");
		if (obj == null || !obj.isHasOptionalProgram()) {
			return ruleProcessorData;
		}
		OptionalProgramMatchRule.processOptionalProgramCourseMatchRule(obj,ruleProcessorData);
		mapOptional.put("FI",obj);
		ruleProcessorData.setMapOptional(mapOptional);
		return ruleProcessorData;
	}

}
