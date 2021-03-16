package ca.bc.gov.educ.api.ruleengine.rule;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.ruleengine.struct.MatchRuleData;
import ca.bc.gov.educ.api.ruleengine.struct.SpecialMatchRuleData;
import ca.bc.gov.educ.api.ruleengine.struct.MinCreditRuleData;
import ca.bc.gov.educ.api.ruleengine.struct.MinElectiveCreditRuleData;
import ca.bc.gov.educ.api.ruleengine.struct.SpecialMinElectiveCreditRuleData;
import ca.bc.gov.educ.api.ruleengine.struct.RuleData;

@Component
public class RuleFactory {

    private static Logger logger = LoggerFactory.getLogger(RuleFactory.class);

    public static Rule createRule(RuleType ruleType, RuleData inputData) {
        switch (ruleType) {
            case MIN_CREDITS:
                return new MinCreditsRule((MinCreditRuleData) inputData);
            case MATCH:
                return new MatchRule((MatchRuleData) inputData);
            case MIN_CREDITS_ELECTIVE:
                return new MinElectiveCreditsRule((MinElectiveCreditRuleData) inputData);
            case SPECIAL_MATCH:
            	return new SpecialMatchRule((SpecialMatchRuleData) inputData);
            case SPECIAL_MIN_CREDITS_ELECTIVE:
            	return new SpecialMinElectiveCreditsRule((SpecialMinElectiveCreditRuleData) inputData);
            default:
                return new MinCreditsRule((MinCreditRuleData) inputData);
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
