package ca.bc.gov.educ.api.ruleengine.rule;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.ruleengine.struct.RuleData;
import ca.bc.gov.educ.api.ruleengine.struct.RuleProcessorData;

@Component
public class RuleFactory {

    private static Logger logger = LoggerFactory.getLogger(RuleFactory.class);

    public static Rule createRule(RuleType ruleType, RuleData inputData) {
        switch (ruleType) {
            case MIN_CREDITS:
                return new MinCreditsRule((RuleProcessorData) inputData);
            case MATCH:
                return new MatchCreditsRule((RuleProcessorData) inputData);
            case MIN_CREDITS_ELECTIVE:
                return new MinElectiveCreditsRule((RuleProcessorData) inputData);
            case SPECIAL_MATCH:
            	return new SpecialMatchRule((RuleProcessorData) inputData);
            case SPECIAL_MIN_CREDITS_ELECTIVE:
            	return new SpecialMinElectiveCreditsRule((RuleProcessorData) inputData);
            default:
                return new MinCreditsRule((RuleProcessorData) inputData);
        }
    }

    public RuleEngine createRuleEngine() {
        return new RuleEngine();
    }

    public RuleEngine createRuleEngine(Rule rule) {
        RuleEngine ruleEngine = new RuleEngine();
        ruleEngine.addRule(rule);
        logger.debug("####Rule: " + ruleEngine.getRules().size());
        return ruleEngine;
    }

    public RuleEngine createRuleEngine(List<Rule> rules) {
        RuleEngine ruleEngine = new RuleEngine();
        ruleEngine.addRules(rules);
        return ruleEngine;
    }
}
