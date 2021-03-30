package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.struct.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class RuleFactory {

    private static final Logger logger = LoggerFactory.getLogger(RuleFactory.class);

    public static Rule createRule(String ruleImplementation, RuleProcessorData data) {
        Class<Rule> clazz;
        Rule rule = null;

        try {
            clazz = (Class<Rule>) Class.forName("ca.bc.gov.educ.api.ruleengine.rule." + ruleImplementation);
            rule = clazz.getDeclaredConstructor(RuleProcessorData.class).newInstance(data);
            System.out.println("Class Created: " + rule.getClass());
        } catch (Exception e) {
            logger.debug("ERROR: No Such Class: " + ruleImplementation);
            logger.debug("Message:" + Arrays.toString(e.getStackTrace()));
        }

        return rule;
    }
}
