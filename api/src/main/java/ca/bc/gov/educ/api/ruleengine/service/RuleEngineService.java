package ca.bc.gov.educ.api.ruleengine.service;

import ca.bc.gov.educ.api.ruleengine.rule.*;
import ca.bc.gov.educ.api.ruleengine.struct.*;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils.parseTraxDate;

@Service
public class RuleEngineService {

    private static final Logger logger = LoggerFactory.getLogger(RuleEngineService.class);

    /**
     * Process all the Grad Algorithm Rules
     *
     * @return RuleProcessorData
     * @exception java.io.IOException
     */
    @SneakyThrows
    public RuleProcessorData processGradAlgorithmRules(RuleProcessorData ruleProcessorData) {

        logger.debug("In Service ProcessGradAlgorithmRules");

        RuleProcessorData originalData = RuleProcessorRuleUtils.cloneObject(ruleProcessorData);
        ruleProcessorData.setGraduated(true);

        for (GradAlgorithmRule gradAlgorithmRule : originalData.getGradAlgorithmRules()) {
            Rule rule = RuleFactory.createRule(gradAlgorithmRule.getRuleImplementation(), ruleProcessorData);
            rule.setInputData(ruleProcessorData);
            ruleProcessorData = (RuleProcessorData)rule.fire();
        }

        return ruleProcessorData;
    }


}
