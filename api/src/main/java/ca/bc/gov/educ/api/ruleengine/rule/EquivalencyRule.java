package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        logger.info("## Equivalency Rule is fired.");

        handleProgramRulesForAssessmentEquivalency();
        handleOptionalProgramRulesForAssessmentEquivalency();

        return ruleProcessorData;
    }

    private void handleProgramRulesForAssessmentEquivalency() {
        List<GradRequirement> requirementsMet = new ArrayList<>();
        logger.info(" # Processing Program Courses for Assessment Equivalency");

        List<StudentCourse> courseList = RuleProcessorRuleUtils.getUniqueStudentCourses(
                ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());

        List<ProgramRequirement> gradProgramRulesMatch = ruleProcessorData.getGradProgramRules()
                .stream()
                .filter(gradProgramRule -> "M".compareTo(gradProgramRule.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
                        && "Y".compareTo(gradProgramRule.getProgramRequirementCode().getActiveRequirement()) == 0
                        && "A".compareTo(gradProgramRule.getProgramRequirementCode().getRequirementCategory()) == 0)
                .collect(Collectors.toList());

        if (courseList == null || courseList.isEmpty()) {
            logger.warn("!!!Empty course list sent to Equivalency Rule for processing");
            return;
        }

        // 1. check assessment is empty or failed
        boolean isEmptyAssessments = false;
        if (ruleProcessorData.getStudentAssessments() == null || ruleProcessorData.getStudentAssessments().isEmpty()) {
            isEmptyAssessments = true;
        }
        boolean hasAnyFailedAssessment = false;
        List<ProgramRequirement> failedRules = gradProgramRulesMatch.stream()
                .filter((pr -> !pr.getProgramRequirementCode().isPassed()))
                .collect(Collectors.toList());
        if (!failedRules.isEmpty()) {
            hasAnyFailedAssessment = true;
        }

        if (!isEmptyAssessments && !hasAnyFailedAssessment) {
            logger.warn("!!!Not empty nor failed assessments -> skip Equivalency Rule for processing");
            return;
        }

        // 2. check courses/exams that meet assessment equivalency requirements.
        List<CourseRequirement> courseRequirements = ruleProcessorData.getCourseRequirements();
        if(courseRequirements == null) {
            courseRequirements = new ArrayList<>();
        }

        List<String> failedRuleCodes = failedRules.stream().map(fr -> fr.getProgramRequirementCode().getProReqCode()).collect(Collectors.toList());

        List<CourseRequirement> courseRequirementsForEquivalency = courseRequirements
                .stream()
                .filter(cr -> failedRuleCodes.contains(cr.getRuleCode().getCourseRequirementCode()))
//                .filter(cr -> "116".compareTo(cr.getRuleCode().getCourseRequirementCode()) == 0
//                            || "115".compareTo(cr.getRuleCode().getCourseRequirementCode()) == 0
//                            || "118".compareTo(cr.getRuleCode().getCourseRequirementCode()) == 0
//                            || "118".compareTo(cr.getRuleCode().getCourseRequirementCode()) == 0
//                            || "303".compareTo(cr.getRuleCode().getCourseRequirementCode()) == 0
//                            || "304".compareTo(cr.getRuleCode().getCourseRequirementCode()) == 0)
                .collect(Collectors.toList());

        List<StudentAssessment> finalAssessmentList = new ArrayList<>();

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
                }
            });
        }

        if (!requirementsMet.isEmpty()) {
            // add new reqsMet
            List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMet();
            if (reqsMet == null)
                reqsMet = new ArrayList<>();
            reqsMet.addAll(requirementsMet);
            ruleProcessorData.setRequirementsMet(reqsMet);

            // remove from nonGradReasons if it exists in reqsMet
            List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasons().stream()
                    .filter(gr -> !ruleProcessorData.getRequirementsMet().contains(gr)).collect(Collectors.toList());
            ruleProcessorData.setNonGradReasons(nonGradReasons);
        }

        if (!finalAssessmentList.isEmpty()) {
            // merge studentAssessments with finalAssessmentList
            // => if any of finalAssessments already exists in studentAssessments, remove the current one and add the new one.
            List<StudentAssessment> tempAssessments = ruleProcessorData.getStudentAssessments().stream()
                    .filter(sa -> !finalAssessmentList.contains(sa)).collect(Collectors.toList());
            tempAssessments.addAll(finalAssessmentList);
            ruleProcessorData.setStudentAssessments(tempAssessments);
        }
    }

    private void processAssessmentEquivalency(StudentCourse studentCourse, ProgramRequirement pr, CourseRequirement cr, String pen, List<GradRequirement> requirementsMet, List<StudentAssessment> finalAssessmentList) {
        if ("Y".compareTo(studentCourse.getProvExamCourse()) == 0 &&
            (studentCourse.getBestExamPercent() != null && studentCourse.getBestExamPercent().doubleValue() > 0)) {
            createAssessmentRecord(pr, cr, pen, requirementsMet, finalAssessmentList);
        }
    }

    private void createAssessmentRecord(ProgramRequirement pr, CourseRequirement cr,  String pen, List<GradRequirement> requirementsMet, List<StudentAssessment> finalAssessmentList) {
        StudentAssessment sA = new StudentAssessment();
        String assessmentCode = cr.getRuleCode().getLabel(); // GRAD2-1744: label keeps the actual assessment code for assessment equivalency.
        sA.setAssessmentCode(assessmentCode);
        sA.setPen(pen);
        ruleProcessorData.getAssessmentList().stream().filter(amt -> assessmentCode.equals(amt.getAssessmentCode())).findAny().ifPresent(asmt -> sA.setAssessmentName(asmt.getAssessmentName()));
        sA.setGradReqMet(pr.getProgramRequirementCode().getTraxReqNumber());
        sA.setGradReqMetDetail(pr.getProgramRequirementCode().getTraxReqNumber() + " - " + pr.getProgramRequirementCode().getLabel());
        sA.setSpecialCase("M");
        sA.setUsed(true);
        sA.setProficiencyScore(Double.valueOf("0"));
        finalAssessmentList.add(sA);
        pr.getProgramRequirementCode().setPassed(true);
        requirementsMet.add(new GradRequirement(pr.getProgramRequirementCode().getTraxReqNumber(), pr.getProgramRequirementCode().getLabel(),pr.getProgramRequirementCode().getProReqCode()));
    }

    private void handleOptionalProgramRulesForAssessmentEquivalency() {
        logger.info(" # Processing Optional Program Courses for Assessment Equivalency");
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
        boolean hasAnyFailedAssessment = false;
        List<OptionalProgramRequirement> failedRules = gradOptionalProgramRulesMatch.stream()
                .filter((opr -> !opr.getOptionalProgramRequirementCode().isPassed()))
                .collect(Collectors.toList());
        if (!failedRules.isEmpty()) {
            hasAnyFailedAssessment = true;
        }

        if (!isEmptyAssessments && !hasAnyFailedAssessment) {
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
                .filter(cr -> failedRuleCodes.contains(cr.getRuleCode().getCourseRequirementCode()))
//                .filter(cr -> "203".compareTo(cr.getRuleCode().getCourseRequirementCode()) == 0
//                            || "403".compareTo(cr.getRuleCode().getCourseRequirementCode()) == 0
//                            || "404".compareTo(cr.getRuleCode().getCourseRequirementCode()) == 0)
                .collect(Collectors.toList());

        List<StudentAssessment> finalAssessmentList = new ArrayList<>();

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
                }
            });
        }

        if (!requirementsMet.isEmpty()) {
            // add new reqsMet
            List<GradRequirement> reqsMet = obj.getRequirementsMetOptionalProgram();
            if (reqsMet == null)
                reqsMet = new ArrayList<>();
            reqsMet.addAll(requirementsMet);
            obj.setRequirementsMetOptionalProgram(reqsMet);

            // remove from nonGradReasons if it exists in reqsMet
            List<GradRequirement> nonGradReasons = obj.getNonGradReasonsOptionalProgram().stream()
                    .filter(gr -> !obj.getRequirementsMetOptionalProgram().contains(gr)).collect(Collectors.toList());
            obj.setNonGradReasonsOptionalProgram(nonGradReasons);
        }

        if (!finalAssessmentList.isEmpty()) {
            // merge studentAssessments with finalAssessmentList
            // => if any of finalAssessments already exists in studentAssessments, remove the current one and add the new one.
            List<StudentAssessment> tempAssessments = obj.getStudentAssessmentsOptionalProgram().stream()
                    .filter(sa -> !finalAssessmentList.contains(sa)).collect(Collectors.toList());
            tempAssessments.addAll(finalAssessmentList);
            obj.setStudentAssessmentsOptionalProgram(tempAssessments);
        }
    }

    private void processAssessmentEquivalencyOptionalProgram(StudentCourse studentCourse, OptionalProgramRequirement opr, CourseRequirement cr, String pen, List<GradRequirement> requirementsMet, List<StudentAssessment> finalAssessmentList) {
        if ("Y".compareTo(studentCourse.getProvExamCourse()) == 0 &&
                (studentCourse.getBestExamPercent() != null && studentCourse.getBestExamPercent().doubleValue() > 0)) {
            createAssessmentRecordOptionalProgram(opr, cr, pen, requirementsMet, finalAssessmentList);
        }
    }

    private void createAssessmentRecordOptionalProgram(OptionalProgramRequirement opr, CourseRequirement cr,  String pen, List<GradRequirement> requirementsMet, List<StudentAssessment> finalAssessmentList) {
        StudentAssessment sA = new StudentAssessment();
        String assessmentCode = cr.getRuleCode().getLabel(); // GRAD2-1744: label keeps the actual assessment code for assessment equivalency.
        sA.setAssessmentCode(assessmentCode);
        sA.setPen(pen);
        ruleProcessorData.getAssessmentList().stream().filter(amt -> assessmentCode.equals(amt.getAssessmentCode())).findAny().ifPresent(asmt -> sA.setAssessmentName(asmt.getAssessmentName()));
        sA.setGradReqMet(opr.getOptionalProgramRequirementCode().getTraxReqNumber());
        sA.setGradReqMetDetail(opr.getOptionalProgramRequirementCode().getTraxReqNumber() + " - " + opr.getOptionalProgramRequirementCode().getLabel());
        sA.setSpecialCase("M");
        sA.setUsed(true);
        sA.setProficiencyScore(Double.valueOf("0"));
        finalAssessmentList.add(sA);
        opr.getOptionalProgramRequirementCode().setPassed(true);
        requirementsMet.add(new GradRequirement(opr.getOptionalProgramRequirementCode().getTraxReqNumber(), opr.getOptionalProgramRequirementCode().getLabel(), opr.getOptionalProgramRequirementCode().getOptProReqCode()));
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("EquivalencyRule: Rule Processor Data set.");
    }
}
