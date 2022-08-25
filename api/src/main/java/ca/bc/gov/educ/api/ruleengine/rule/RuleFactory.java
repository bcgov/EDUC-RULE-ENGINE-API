package ca.bc.gov.educ.api.ruleengine.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class RuleFactory {

    private static final Logger logger = LoggerFactory.getLogger(RuleFactory.class);

    private RuleFactory() {}

    @SuppressWarnings("unchecked")
	public static Rule createRule(String ruleImplementation) {
        Class<Rule> clazz;
        Rule rule = null;

        try {
            clazz = (Class<Rule>) Class.forName("ca.bc.gov.educ.api.ruleengine.rule." + ruleImplementation);
            rule = clazz.getDeclaredConstructor().newInstance();
            logger.debug("Class Created: {}" , rule.getClass());
        } catch (Exception e) {
            logger.debug("ERROR: No Such Class: {}" , ruleImplementation);
            logger.debug("Message: {}" , Arrays.toString(e.getStackTrace()));
        }

        return rule;
    }
}
