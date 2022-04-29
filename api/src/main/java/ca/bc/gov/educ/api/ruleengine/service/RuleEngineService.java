package ca.bc.gov.educ.api.ruleengine.service;

import ca.bc.gov.educ.api.ruleengine.dto.ProgramAlgorithmRule;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.rule.Rule;
import ca.bc.gov.educ.api.ruleengine.rule.RuleFactory;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;

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

        try {
            String ruleData = new ObjectMapper().writeValueAsString(originalData);
            FileWriter myWriter = new FileWriter("filename.txt");
            myWriter.write(ruleData);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        for (ProgramAlgorithmRule gradAlgorithmRule : originalData.getAlgorithmRules()) {
        	Rule rule = RuleFactory.createRule(gradAlgorithmRule.getAlgorithmRuleCode().getRuleImplementation(), ruleProcessorData);
            rule.setInputData(ruleProcessorData);
            ruleProcessorData = (RuleProcessorData)rule.fire();
        }
        if(ruleProcessorData.getNonGradReasons() == null || ruleProcessorData.getNonGradReasons().isEmpty()) {
            if(ruleProcessorData.getRequirementsMet() != null && !ruleProcessorData.getRequirementsMet().isEmpty()) {
                ruleProcessorData.setGraduated(true);
            }else {
                ruleProcessorData.setGraduated(false);
            }
        }else {
            ruleProcessorData.setGraduated(false);
        }
        return ruleProcessorData;
    }

}
