package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.OptionalProgramRuleProcessor;
import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentFrenchImmersionMatchCreditsRule implements Rule {

	private static Logger logger = LoggerFactory.getLogger(AssessmentFrenchImmersionMatchCreditsRule.class);

	@Autowired
	private RuleProcessorData ruleProcessorData;


	public RuleData fire() {
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

	@Override
	public void setInputData(RuleData inputData) {
		ruleProcessorData = (RuleProcessorData) inputData;
		logger.info("AssessmentFrenchImmersionMatchCreditsRule: Rule Processor Data set.");
	}

}
