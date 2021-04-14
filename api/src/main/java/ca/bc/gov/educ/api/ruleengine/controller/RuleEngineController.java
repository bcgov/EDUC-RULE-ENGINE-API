package ca.bc.gov.educ.api.ruleengine.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.bc.gov.educ.api.ruleengine.service.RuleEngineService;
import ca.bc.gov.educ.api.ruleengine.struct.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.util.PermissionsContants;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiConstants;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@CrossOrigin
@RestController
@RequestMapping (RuleEngineApiConstants.RULE_ENGINE_API_ROOT_MAPPING)
@EnableResourceServer
@OpenAPIDefinition(info = @Info(title = "API for Rule Engine.", description = "This API is for Rule Engine.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"RUN_RULE_ENGINE"})})
public class RuleEngineController {

    private static final Logger logger = LoggerFactory.getLogger(RuleEngineController.class);

    @Autowired
    RuleEngineService ruleEngineService;

    @PostMapping ("/run-grad-algorithm-rules")
    @PreAuthorize(PermissionsContants.RUN_RULE_ENGINE)
    public RuleProcessorData processGradAlgorithmRules(@RequestBody RuleProcessorData ruleProcessorData) {
        logger.debug("**** Processing Grad Algorithm Rules");
        return ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
    }
}
