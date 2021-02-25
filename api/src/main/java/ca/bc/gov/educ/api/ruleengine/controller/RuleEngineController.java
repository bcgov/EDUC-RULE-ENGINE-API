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
import ca.bc.gov.educ.api.ruleengine.struct.MatchRuleData;
import ca.bc.gov.educ.api.ruleengine.struct.MinCreditRuleData;
import ca.bc.gov.educ.api.ruleengine.struct.MinElectiveCreditRuleData;
import ca.bc.gov.educ.api.ruleengine.struct.RuleData;
import ca.bc.gov.educ.api.ruleengine.struct.StudentCourses;
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

    @Autowired
    MinCreditRuleData minCreditRuleData;

    @PostMapping ("/find-not-completed")
    @PreAuthorize(PermissionsContants.RUN_RULE_ENGINE)
    public StudentCourses findNotCompletedCourses(@RequestBody StudentCourses studentCourses) {
        logger.debug("**** Mark NOT COMPLETED");
        return ruleEngineService.findAllIncompleteCourses(studentCourses);
    }

    @PostMapping ("/find-projected")
    @PreAuthorize(PermissionsContants.RUN_RULE_ENGINE)
    public StudentCourses findProjectedCourses(@RequestBody StudentCourses studentCourses) {
        logger.debug("**** Mark PROJECTED");
        return ruleEngineService.findAllProjectedCourses(studentCourses);
    }

    @PostMapping ("/find-failed")
    @PreAuthorize(PermissionsContants.RUN_RULE_ENGINE)
    public StudentCourses findFailedCourses(@RequestBody StudentCourses studentCourses) {
        logger.debug("**** Mark FAILED");
        return ruleEngineService.findAllFailedCourses(studentCourses);
    }

    @PostMapping ("/find-duplicates")
    @PreAuthorize(PermissionsContants.RUN_RULE_ENGINE)
    public StudentCourses findDuplicateCourses(@RequestBody StudentCourses studentCourses) {
        logger.debug("**** Mark DUPLICATES");
        return ruleEngineService.findAllDuplicateCourses(studentCourses);
    }

    @PostMapping ("/run-min-credits-rules")
    @PreAuthorize(PermissionsContants.RUN_RULE_ENGINE)
    public RuleData runMinCreditsRule(@RequestBody MinCreditRuleData minCreditRuleInput) {
        logger.debug("**** Running MinCreditsRule");
        logger.debug("****MinCreditRuleData: " + minCreditRuleInput);
        return ruleEngineService.runMinCreditsRule(minCreditRuleInput);
    }

    
    @PostMapping ("/run-match-rules")
    @PreAuthorize(PermissionsContants.RUN_RULE_ENGINE)
    public RuleData runMatchRules(@RequestBody MatchRuleData matchRuleInput) {
        logger.debug("**** Running MatchRules");
        return ruleEngineService.runMatchRules(matchRuleInput);
    }

    @PostMapping ("/run-min-elective-credits-rules")
    @PreAuthorize(PermissionsContants.RUN_RULE_ENGINE)
    public RuleData runMinElectiveCreditsRule(@RequestBody MinElectiveCreditRuleData minElectiveCreditRuleInput) {
        logger.debug("**** Running MinElectiveCreditsRule");
        return ruleEngineService.runMinElectiveCreditsRule(minElectiveCreditRuleInput);
    }

    /*
    @GetMapping(RuleEngineApiConstants.GET_PROGRAM_RULE_BY_REQ_CODE_MAPPING)
    public ProgramRule getProgramRuleByReqCode(@PathVariable int requirementCode) {
        logger.debug("#Get Program Rule by Requirement Code: " + requirementCode);
        return ruleEngineService.getProgramRuleByRequirementCode(requirementCode);
    }

    @PostMapping(RuleEngineApiConstants.API_ROOT_MAPPING)
    public ProgramRule createProgramRule(@RequestBody ProgramRule programRule) {
        logger.debug("#Create a new Program Rule: " + programRule.getRequirementCode());
        logger.debug("******Program Rule*****\n" + programRule.toString());
        return ruleEngineService.createProgramRule(programRule);
    }

    @PutMapping(RuleEngineApiConstants.API_ROOT_MAPPING)
    public ProgramRule updateProgramRule(@Validated @RequestBody ProgramRule programRule)  {
        logger.debug("#Update a Program Rule: " + programRule.getRequirementCode());
        logger.debug("******Program Rule*****\n" + programRule.toString());
        return ruleEngineService.updateProgramRule(programRule);
    }
     */
}
