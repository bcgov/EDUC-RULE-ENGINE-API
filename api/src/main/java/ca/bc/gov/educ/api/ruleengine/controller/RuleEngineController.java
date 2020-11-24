package ca.bc.gov.educ.api.ruleengine.controller;

import ca.bc.gov.educ.api.ruleengine.service.RuleEngineService;
import ca.bc.gov.educ.api.ruleengine.struct.MinCreditRuleData;
import ca.bc.gov.educ.api.ruleengine.struct.StudentCourses;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping (RuleEngineApiConstants.RULE_ENGINE_API_ROOT_MAPPING)
public class RuleEngineController {

    private static final Logger logger = LoggerFactory.getLogger(RuleEngineController.class);

    @Autowired
    RuleEngineService ruleEngineService;

    @Autowired
    MinCreditRuleData minCreditRuleData;

    @PostMapping ("/find-not-completed")
    public StudentCourses findNotCompletedCourses(@RequestBody StudentCourses studentCourses) {
        logger.debug("**** Mark NOT COMPLETED");
        return ruleEngineService.findAllIncompleteCourses(studentCourses);
    }

    @PostMapping ("/find-failed")
    public StudentCourses findFailedCourses(@RequestBody StudentCourses studentCourses) {
        logger.debug("**** Mark FAILED");
        return ruleEngineService.findAllFailedCourses(studentCourses);
    }

    @PostMapping ("/find-duplicates")
    public StudentCourses findDuplicateCourses(@RequestBody StudentCourses studentCourses) {
        logger.debug("**** Mark DUPLICATES");
        return ruleEngineService.findAllDuplicateCourses(studentCourses);
    }

    @PostMapping ("/run-mincredits")
    public boolean runMinCreditsRule(@RequestBody MinCreditRuleData minCreditRuleData) {
        logger.debug("**** Running MinCreditsRule");
        return ruleEngineService.runMinCreditsRule(minCreditRuleData);
    }

    @PostMapping ("/run-matchcredits")
    public boolean runMatchCreditsRule(@RequestBody MinCreditRuleData minCreditRuleData) {
        logger.debug("**** Running runMatchCreditsRule");
        return ruleEngineService.runMatchCreditsRule(minCreditRuleData);
    }

    @PostMapping ("/run-minelectivecredits")
    public boolean runMinElectiveCreditsRule(@RequestBody MinCreditRuleData minCreditRuleData) {
        logger.debug("**** Running runMinElectiveCreditsRule");
        return ruleEngineService.runMinElectiveCreditsRule(minCreditRuleData);
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
