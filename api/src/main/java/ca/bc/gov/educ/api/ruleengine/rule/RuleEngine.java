package ca.bc.gov.educ.api.ruleengine.rule;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import lombok.Data;

@Data
@Component
public class  RuleEngine {

    private static Logger logger = LoggerFactory.getLogger(RuleEngine.class);

    private List<Rule> rules = new ArrayList<Rule>();

    public RuleEngine() {}

    public void addRule(Rule rule) {
        rules.add(rule);
    }

    public void addRules(List<Rule> rules) {
        rules.addAll(rules);
    }

    public RuleData fireRules() {
        RuleData outcome = null;

        if (rules == null || rules.size() == 0) {
            logger.debug("!!!!Rules empty!!!!");
            return outcome;
        }
        else {
            for (Rule rule : rules) {
                outcome = rule.fire();
            }
        }
        return outcome;
    }
}
