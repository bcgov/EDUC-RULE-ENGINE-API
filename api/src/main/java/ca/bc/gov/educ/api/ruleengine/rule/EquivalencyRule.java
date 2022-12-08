package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class EquivalencyRule implements Rule {
    private static Logger logger = LoggerFactory.getLogger(EquivalencyRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {
        logger.debug(" # Equivalency Rule processor is  for Assessment Equivalency");
        handleProgramRulesForAssessmentEquivalency();
        handleOptionalProgramRulesForAssessmentEquivalency();
        return ruleProcessorData;
    }

    private void handleProgramRulesForAssessmentEquivalency() {
        logger.info("# Processing Program Rules for Assessment Equivalency");

        List<StudentCourse> courseList = RuleProcessorRuleUtils.getUniqueStudentCourses(
                ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());
        if (courseList == null || courseList.isEmpty()) {
            logger.warn("!!!Empty course list sent to Equivalency Rule for processing");
            return;
        }

        List<GradRequirement> requirementsMet = new ArrayList<>();
        logger.debug("Total Program rules list size: {}", ruleProcessorData.getGradProgramRules().size());
        List<ProgramRequirement> gradProgramRulesMatch = ruleProcessorData.getGradProgramRules()
                .stream()
                .filter(gradProgramRule -> "M".compareTo(gradProgramRule.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
                        && "Y".compareTo(gradProgramRule.getProgramRequirementCode().getActiveRequirement()) == 0
                        && "A".compareTo(gradProgramRule.getProgramRequirementCode().getRequirementCategory()) == 0)
                .collect(Collectors.toList());
        logger.debug("Matched Program rules list size {}", gradProgramRulesMatch.size());

        // 1. check assessment is empty or failed
        boolean isEmptyAssessments = false;
        if (ruleProcessorData.getStudentAssessments() == null || ruleProcessorData.getStudentAssessments().isEmpty()) {
            isEmptyAssessments = true;
        }
        List<ProgramRequirement> failedRulesForAssessment = gradProgramRulesMatch.stream()
                .filter((pr -> !pr.getProgramRequirementCode().isPassed()))
                .collect(Collectors.toList());
        if (!isEmptyAssessments && failedRulesForAssessment.isEmpty()) {
            logger.warn("!!!Not empty nor failed assessments -> skip Equivalency Rule for processing");
            return;
        }

        // 2. check courses/exams that meet assessment equivalency requirements.
        List<CourseRequirement> courseRequirements = ruleProcessorData.getCourseRequirements();
        if(courseRequirements == null) {
            courseRequirements = new ArrayList<>();
        }

        List<String> failedRuleCodes = failedRulesForAssessment.stream().map(fr -> fr.getProgramRequirementCode().getProReqCode()).collect(Collectors.toList());
        List<CourseRequirement> courseRequirementsForEquivalency = courseRequirements
                .stream()
                .filter(cr -> failedRuleCodes.contains(cr.getRuleCode().getCourseRequirementCode())) // Rule# 115,116,118,303,304
                .collect(Collectors.toList());

        List<StudentAssessment> finalAssessmentList = new ArrayList<>();
        List<ProgramRequirement> finalProgramRulesList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        for (StudentCourse st : courseList) {
            List<CourseRequirement> matchedCourseRequirements = courseRequirementsForEquivalency.stream()
                    .filter(equivCr -> equivCr.getCourseCode().compareTo(st.getCourseCode()) == 0
                            && equivCr.getCourseLevel().compareTo(st.getCourseLevel()) == 0)
                    .collect(Collectors.toList());
            matchedCourseRequirements.stream().forEach(courseRequirement -> {
                ProgramRequirement programRule = null;
                if (courseRequirement != null) {
                    programRule = gradProgramRulesMatch.stream()
                            .filter(pr -> pr.getProgramRequirementCode().getProReqCode().compareTo(courseRequirement.getRuleCode().getCourseRequirementCode()) == 0)
                            .findAny()
                            .orElse(null);
                }
                if (programRule != null && !programRule.getProgramRequirementCode().isPassed()) {
                    // process course to meet the requirement
                    logger.info("Pseudo Assessment ==> Program rule[{}] - course code[{}] / [{}]", programRule.getProgramRequirementCode().getProReqCode(), st.getCourseCode(), st.getCourseLevel());
                    processAssessmentEquivalency(st, programRule, courseRequirement, ruleProcessorData.getGradStudent().getPen(), requirementsMet, finalAssessmentList);
                    try {
                        ProgramRequirement tempPR = objectMapper.readValue(objectMapper.writeValueAsString(programRule), ProgramRequirement.class);
                        if (tempPR != null && !finalProgramRulesList.contains(tempPR)) {
                            finalProgramRulesList.add(tempPR);
                        }
                        logger.debug("TempPR: {}",tempPR);
                        logger.debug("Final Program rules list size: {}",finalProgramRulesList.size());
                    } catch (IOException e) {
                        logger.error("ERROR: {}",e.getMessage());
                    }
                }
            });
        }
        logger.debug("Final Program rules list size 1: {}",finalProgramRulesList.size());

        handleFailedRules(finalProgramRulesList, requirementsMet, gradProgramRulesMatch);

        if (!finalAssessmentList.isEmpty()) {
            // merge studentAssessments with finalAssessmentList
            // => if any of finalAssessments already exists in studentAssessments, remove the current one and add the new one.
            List<StudentAssessment> tempAssessments = ruleProcessorData.getStudentAssessments().stream()
                    .filter(sa -> !finalAssessmentList.contains(sa)).collect(Collectors.toList());
            List<StudentAssessment> processedAssessments = new ArrayList<>();
            finalAssessmentList.stream().forEach(sa -> {
                try {
                    StudentAssessment tempSA = objectMapper.readValue(objectMapper.writeValueAsString(sa), StudentAssessment.class);
                    if (tempSA != null)
                        processedAssessments.add(tempSA);
                    logger.debug("TempSC: {}",tempSA);
                    logger.debug("Final Assessment List size: : {}",processedAssessments.size());
                } catch (IOException e) {
                    logger.error("ERROR: {}",e.getMessage());
                }
            });
            tempAssessments.addAll(processedAssessments);
            ruleProcessorData.setStudentAssessments(tempAssessments);
        }

        logger.debug("Final Program rules list size 2: {}",finalProgramRulesList.size());

        //finalProgramRulesList only has the Match type rules in it. Add rest of the type of rules back to the list.
        finalProgramRulesList.addAll(ruleProcessorData.getGradProgramRules()
                .stream()
                .filter(gradProgramRule -> "M".compareTo(gradProgramRule.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) != 0
                        || "A".compareTo(gradProgramRule.getProgramRequirementCode().getRequirementCategory()) != 0)
                .collect(Collectors.toList()));

        logger.debug("Final Program rules list size 3: {}",finalProgramRulesList.size());
        ruleProcessorData.setGradProgramRules(finalProgramRulesList);
        logger.debug("Final Total Program rules list size: {}", ruleProcessorData.getGradProgramRules().size());
    }

    private void handleFailedRules(List<ProgramRequirement> finalProgramRulesList, List<GradRequirement> requirementsMet, List<ProgramRequirement> gradProgramRulesMatch) {
        List<ProgramRequirement> successfulRules = finalProgramRulesList.stream()
                .filter(pr -> pr.getProgramRequirementCode().isPassed())
                .collect(Collectors.toList());

        if(gradProgramRulesMatch.size() != finalProgramRulesList.size()) {
            List<ProgramRequirement> unusedRules = RuleEngineApiUtils.getCloneProgramRule(gradProgramRulesMatch);
            unusedRules.removeAll(finalProgramRulesList);
            finalProgramRulesList.addAll(unusedRules);
        }

        List<ProgramRequirement> failedRules = finalProgramRulesList.stream()
                .filter(pr -> !pr.getProgramRequirementCode().isPassed()).collect(Collectors.toList());


        if (failedRules.isEmpty()) {
            logger.debug("All the failed assessment match rules met the assessment equivalency requirement!");
            ruleProcessorData.setGraduated(true);
        } else {
            // no need to add the failed one into requirementsNotMet as it was processed as failed before in assessment related rule processors
            logger.info("One or more Match rules did not meet the assessment equivalency requirement!");
            ruleProcessorData.setGraduated(false);
        }

        // if any failed assessments from the previous processors are successful here, remove it's nonGradReason
        if (!successfulRules.isEmpty()) {
            List<String> successfulRuleCodes = successfulRules.stream().map(pr -> pr.getProgramRequirementCode().getProReqCode()).collect(Collectors.toList());
            List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasons().stream()
                    .filter(gr -> !successfulRuleCodes.contains(gr.getRule())).collect(Collectors.toList());
            ruleProcessorData.setNonGradReasons(nonGradReasons);
        }

        // add new requirements met
        if (!requirementsMet.isEmpty()) {
            List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMet();
            if (reqsMet == null)
                reqsMet = new ArrayList<>();
            reqsMet.addAll(requirementsMet);
            ruleProcessorData.setRequirementsMet(reqsMet);
        }

    }

    private void processAssessmentEquivalency(StudentCourse studentCourse, ProgramRequirement pr, CourseRequirement cr, String pen, List<GradRequirement> requirementsMet, List<StudentAssessment> finalAssessmentList) {
        if ("Y".compareTo(studentCourse.getProvExamCourse()) == 0 &&
            (studentCourse.getBestExamPercent() != null && studentCourse.getBestExamPercent().doubleValue() > 0)) {
            AlgorithmSupportRule.createAssessmentRecord(finalAssessmentList, cr.getRuleCode().getLabel(), // GRAD2-1744: label keeps the actual assessment code for assessment equivalency.
                ruleProcessorData.getAssessmentList(), pr, pen, requirementsMet);
        }
    }

    private void handleOptionalProgramRulesForAssessmentEquivalency() {
        logger.info("# Processing Optional Program Rules for Assessment Equivalency");
        Map<String, OptionalProgramRuleProcessor> mapOptional = ruleProcessorData.getMapOptional();

        boolean isFrenchImmersion = ruleProcessorData.getGradProgram().getProgramCode().endsWith("EN");
        OptionalProgramRuleProcessor obj = mapOptional.get(isFrenchImmersion? "FI" : "DD");
        if (obj == null || !obj.isHasOptionalProgram()) {
            return;
        }

        processOptionalProgramRulesForAssessmentEquivalency(obj);

        mapOptional.put(isFrenchImmersion? "FI" : "DD", obj);
        ruleProcessorData.setMapOptional(mapOptional);
    }

    private void processOptionalProgramRulesForAssessmentEquivalency(OptionalProgramRuleProcessor obj) {
        if (!obj.isOptionalProgramGraduated()) {
            obj.setOptionalProgramGraduated(true);
        }
        List<GradRequirement> requirementsMet = new ArrayList<>();

        List<StudentCourse> courseList = RuleProcessorRuleUtils.getUniqueStudentCourses(
                obj.getStudentCoursesOptionalProgram(), ruleProcessorData.isProjected());

        List<OptionalProgramRequirement> gradOptionalProgramRulesMatch = obj
                .getOptionalProgramRules().stream()
                .filter(gradOptionalProgramRule -> "M".compareTo(gradOptionalProgramRule.getOptionalProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
                        && "Y".compareTo(gradOptionalProgramRule.getOptionalProgramRequirementCode().getActiveRequirement()) == 0
                        && "A".compareTo(gradOptionalProgramRule.getOptionalProgramRequirementCode().getRequirementCategory()) == 0)
                .collect(Collectors.toList());

        // 1. check assessment is empty or failed
        boolean isEmptyAssessments = false;
        List<StudentAssessment> studentAssessments = RuleProcessorRuleUtils.getUniqueStudentAssessments(
                obj.getStudentAssessmentsOptionalProgram(), ruleProcessorData.isProjected());
        if (studentAssessments.isEmpty()) {
            isEmptyAssessments = true;
        }
        List<OptionalProgramRequirement> failedRules = gradOptionalProgramRulesMatch.stream()
                .filter((opr -> !opr.getOptionalProgramRequirementCode().isPassed()))
                .collect(Collectors.toList());
        if (!isEmptyAssessments && failedRules.isEmpty()) {
            logger.warn("!!!Not empty nor failed assessments -> skip Equivalency Rule for processing");
            return;
        }

        // 2. check optional program courses/exams that meet assessment equivalency requirements.
        List<CourseRequirement> courseRequirements = ruleProcessorData.getCourseRequirements();
        if(courseRequirements == null) {
            courseRequirements = new ArrayList<>();
        }

        List<String> failedRuleCodes = failedRules.stream().map(fr -> fr.getOptionalProgramRequirementCode().getOptProReqCode()).collect(Collectors.toList());

        List<CourseRequirement> courseRequirementsForEquivalency = courseRequirements
                .stream()
                .filter(cr -> failedRuleCodes.contains(cr.getRuleCode().getCourseRequirementCode())) // Rule# 203, 403, 404
                .collect(Collectors.toList());

        List<StudentAssessment> finalAssessmentList = new ArrayList<>();
        List<OptionalProgramRequirement> finalOptionalProgramRulesList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        for (StudentCourse st : courseList) {
            List<CourseRequirement> matchedCourseRequirements = courseRequirementsForEquivalency.stream()
                    .filter(equivCr -> equivCr.getCourseCode().compareTo(st.getCourseCode()) == 0
                                    && equivCr.getCourseLevel().compareTo(st.getCourseLevel()) == 0)
                    .collect(Collectors.toList());
            matchedCourseRequirements.stream().forEach(courseRequirement -> {
                OptionalProgramRequirement optionalProgramRule = null;
                if (courseRequirement != null) {
                    optionalProgramRule = gradOptionalProgramRulesMatch.stream()
                            .filter(opr -> opr.getOptionalProgramRequirementCode().getOptProReqCode().compareTo(courseRequirement.getRuleCode().getCourseRequirementCode()) == 0)
                            .findAny()
                            .orElse(null);
                }
                if (optionalProgramRule != null && !optionalProgramRule.getOptionalProgramRequirementCode().isPassed()) {
                    // process course to meet the requirement
                    logger.info("Pseudo Assessment ==> Optional Program rule[{}] - course code[{}] / [{}]", optionalProgramRule.getOptionalProgramRequirementCode().getOptProReqCode(), st.getCourseCode(), st.getCourseLevel());
                    processAssessmentEquivalencyOptionalProgram(st, optionalProgramRule, courseRequirement, ruleProcessorData.getGradStudent().getPen(), requirementsMet, finalAssessmentList);
                    try {
                        OptionalProgramRequirement tempSPR = objectMapper.readValue(objectMapper.writeValueAsString(optionalProgramRule),
                                OptionalProgramRequirement.class);
                        if (tempSPR != null)
                            finalOptionalProgramRulesList.add(tempSPR);
                        logger.debug("TempPR: {}", tempSPR);
                        logger.debug("Final Program rules list size: {}", finalOptionalProgramRulesList.size());
                    } catch (IOException e) {
                        logger.error("ERROR:{}", e.getMessage());
                    }
                }
            });
        }

        handleFailedRules(obj, finalOptionalProgramRulesList, requirementsMet, gradOptionalProgramRulesMatch);

        if (!finalAssessmentList.isEmpty()) {
            // merge studentAssessments with finalAssessmentList
            // => if any of finalAssessments already exists in studentAssessments, remove the current one and add the new one.
            List<StudentAssessment> tempAssessments = obj.getStudentAssessmentsOptionalProgram().stream()
                    .filter(sa -> !finalAssessmentList.contains(sa)).collect(Collectors.toList());
            List<StudentAssessment> processedAssessments = new ArrayList<>();
            finalAssessmentList.stream().forEach(sa -> {
                try {
                    StudentAssessment tempSA = objectMapper.readValue(objectMapper.writeValueAsString(sa), StudentAssessment.class);
                    if (tempSA != null)
                        processedAssessments.add(tempSA);
                    logger.debug("TempSC: {}",tempSA);
                    logger.debug("Final Assessment List size: : {}",processedAssessments.size());
                } catch (IOException e) {
                    logger.error("ERROR: {}",e.getMessage());
                }
            });
            tempAssessments.addAll(processedAssessments);
            obj.setStudentAssessmentsOptionalProgram(tempAssessments);
        }
    }

    private void processAssessmentEquivalencyOptionalProgram(StudentCourse studentCourse, OptionalProgramRequirement opr, CourseRequirement cr, String pen, List<GradRequirement> requirementsMet, List<StudentAssessment> finalAssessmentList) {
        if ("Y".compareTo(studentCourse.getProvExamCourse()) == 0 &&
                (studentCourse.getBestExamPercent() != null && studentCourse.getBestExamPercent().doubleValue() > 0)) {
            AlgorithmSupportRule.createAssessmentRecordOptionalProgram(finalAssessmentList, cr.getRuleCode().getLabel(), // GRAD2-1744: label keeps the actual assessment code for assessment equivalency.
                    ruleProcessorData.getAssessmentList(), opr, pen, requirementsMet);
        }
    }

    private void handleFailedRules(OptionalProgramRuleProcessor obj, List<OptionalProgramRequirement> finalOptionalProgramRulesList, List<GradRequirement> requirementsMet, List<OptionalProgramRequirement> gradOptionalProgramRulesMatch) {
        List<OptionalProgramRequirement> successfulRules = finalOptionalProgramRulesList.stream()
                .filter(opr -> opr.getOptionalProgramRequirementCode().isPassed())
                .collect(Collectors.toList());

        if(gradOptionalProgramRulesMatch.size() != finalOptionalProgramRulesList.size()) {
            List<OptionalProgramRequirement> unusedRules = RuleEngineApiUtils.getCloneOptionalProgramRule(gradOptionalProgramRulesMatch);
            unusedRules.removeAll(finalOptionalProgramRulesList);
            finalOptionalProgramRulesList.addAll(unusedRules);
        }

        List<OptionalProgramRequirement> failedRules = finalOptionalProgramRulesList.stream()
                .filter(opr -> !opr.getOptionalProgramRequirementCode().isPassed()).collect(Collectors.toList());

        if (failedRules.isEmpty()) {
            logger.debug("All the failed assessment match rules met the assessment equivalency requirement!");
//            obj.setOptionalProgramGraduated(true);
        } else {
            // no need to add the failed one into requirementsNotMet as it was processed as failed before in assessment related rule processors
            logger.info("One or more Match rules did not meet the assessment equivalency requirement!");
            obj.setOptionalProgramGraduated(false);
        }

        // if any failed assessments from the previous processors are successful here, remove it's nonGradReason
        if (!successfulRules.isEmpty()) {
            List<String> successfulRuleCodes = successfulRules.stream().map(opr -> opr.getOptionalProgramRequirementCode().getOptProReqCode()).collect(Collectors.toList());
            List<GradRequirement> nonGradReasons = obj.getNonGradReasonsOptionalProgram().stream()
                    .filter(gr -> !successfulRuleCodes.contains(gr.getRule())).collect(Collectors.toList());
            obj.setNonGradReasonsOptionalProgram(nonGradReasons);
        }

        // add new requirements met
        if (!requirementsMet.isEmpty()) {
            List<GradRequirement> reqsMet = obj.getRequirementsMetOptionalProgram();
            if (reqsMet == null)
                reqsMet = new ArrayList<>();
            reqsMet.addAll(requirementsMet);
            obj.setRequirementsMetOptionalProgram(reqsMet);
        }

    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("EquivalencyRule: Rule Processor Data set.");
    }
}
