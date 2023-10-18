package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class AlgorithmSupportRule {
    private static final Logger logger = LoggerFactory.getLogger(AlgorithmSupportRule.class);
    private static final String ERROR_FORMAT_STR = "ERROR: {}";

    private AlgorithmSupportRule() {
    }

    public static void processEmptyCourseCondition(RuleProcessorData ruleProcessorData,List<ProgramRequirement> gradProgramRulesMatch, List<GradRequirement> requirementsNotMet) {
        List<StudentCourse> courseList = RuleProcessorRuleUtils.getUniqueStudentCourses(
                ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());
        ruleProcessorData.setStudentCourses(courseList);
        List<ProgramRequirement> failedRules = gradProgramRulesMatch.stream()
                .filter(pr -> !pr.getProgramRequirementCode().isPassed()
                        && pr.getProgramRequirementCode().getRequirementCategory() != null
                        && pr.getProgramRequirementCode().getRequirementCategory().equalsIgnoreCase("C")
                        && "N/A".compareToIgnoreCase(pr.getProgramRequirementCode().getNotMetDesc()) != 0)
                .toList();

        if (failedRules.isEmpty()) {
            logger.debug("All the match rules met!");
        } else {
            for (ProgramRequirement failedRule : failedRules) {
                requirementsNotMet.add(new GradRequirement(failedRule.getProgramRequirementCode().getTraxReqNumber(), failedRule.getProgramRequirementCode().getNotMetDesc(),failedRule.getProgramRequirementCode().getProReqCode()));
            }

            logger.debug("One or more Match rules not met!");
            ruleProcessorData.setGraduated(false);

            List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasons();

            if (nonGradReasons == null)
                nonGradReasons = new ArrayList<>();

            nonGradReasons.addAll(requirementsNotMet);
            ruleProcessorData.setNonGradReasons(nonGradReasons);
        }

        if(ruleProcessorData.getStudentCourses() == null || ruleProcessorData.getStudentCourses().isEmpty()) {
            ruleProcessorData.setStudentCourses(ruleProcessorData.getExcludedCourses());
        }else {
            ruleProcessorData.getStudentCourses().addAll(ruleProcessorData.getExcludedCourses());
        }
    }

    public static void processEmptyAssessmentCondition(RuleProcessorData ruleProcessorData,List<ProgramRequirement> gradProgramRulesMatch, List<GradRequirement> requirementsNotMet) {
        List<StudentCourse> courseList = RuleProcessorRuleUtils.getUniqueStudentCourses(
                ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());
        ruleProcessorData.setStudentCourses(courseList);
        List<ProgramRequirement> failedRules = gradProgramRulesMatch.stream()
                .filter(pr -> !pr.getProgramRequirementCode().isPassed() && pr.getProgramRequirementCode().getRequirementCategory().equalsIgnoreCase("A")).toList();

        if (failedRules.isEmpty()) {
            logger.debug("All the match rules met!");
        } else {
            for (ProgramRequirement failedRule : failedRules) {
                requirementsNotMet.add(new GradRequirement(failedRule.getProgramRequirementCode().getTraxReqNumber(), failedRule.getProgramRequirementCode().getNotMetDesc(),failedRule.getProgramRequirementCode().getProReqCode()));
            }

            logger.debug("One or more Match rules not met!");
            ruleProcessorData.setGraduated(false);

            List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasons();

            if (nonGradReasons == null)
                nonGradReasons = new ArrayList<>();

            nonGradReasons.addAll(requirementsNotMet);
            ruleProcessorData.setNonGradReasons(nonGradReasons);
        }

        if(ruleProcessorData.getStudentAssessments() == null || ruleProcessorData.getStudentAssessments().isEmpty()) {
            ruleProcessorData.setStudentAssessments(ruleProcessorData.getExcludedAssessments());
        }else {
            ruleProcessorData.getStudentAssessments().addAll(ruleProcessorData.getExcludedAssessments());
        }
    }

    public static void createAssessmentRecord(List<StudentAssessment> finalAssessmentList, String aCode, List<Assessment> assmList, ProgramRequirement pr, String pen, List<GradRequirement> requirementsMet) {
        StudentAssessment sA = new StudentAssessment();
        sA.setAssessmentCode(aCode);
        sA.setPen(pen);
        assmList.stream().filter(amt -> aCode.equals(amt.getAssessmentCode())).findAny().ifPresent(asmt -> sA.setAssessmentName(asmt.getAssessmentName()));
        sA.setGradReqMet(pr.getProgramRequirementCode().getTraxReqNumber());
        sA.setGradReqMetDetail(pr.getProgramRequirementCode().getTraxReqNumber() + " - " + pr.getProgramRequirementCode().getLabel());
        sA.setSpecialCase("M");
        sA.setUsed(true);
        sA.setProficiencyScore(Double.valueOf("0"));
        finalAssessmentList.add(sA);
        pr.getProgramRequirementCode().setPassed(true);
        requirementsMet.add(new GradRequirement(pr.getProgramRequirementCode().getTraxReqNumber(), pr.getProgramRequirementCode().getLabel(),pr.getProgramRequirementCode().getProReqCode()));

    }

    public static void createAssessmentRecordOptionalProgram(List<StudentAssessment> finalAssessmentList, String aCode, List<Assessment> assmList, OptionalProgramRequirement pr, String pen, List<GradRequirement> requirementsMet) {
        StudentAssessment sA = new StudentAssessment();
        sA.setAssessmentCode(aCode);
        sA.setPen(pen);
        assmList.stream().filter(amt -> aCode.equals(amt.getAssessmentCode())).findAny().ifPresent(asmt -> sA.setAssessmentName(asmt.getAssessmentName()));
        sA.setGradReqMet(pr.getOptionalProgramRequirementCode().getTraxReqNumber());
        sA.setGradReqMetDetail(pr.getOptionalProgramRequirementCode().getTraxReqNumber() + " - " + pr.getOptionalProgramRequirementCode().getLabel());
        sA.setSpecialCase("M");
        sA.setUsed(true);
        sA.setProficiencyScore(Double.valueOf("0"));
        finalAssessmentList.add(sA);
        pr.getOptionalProgramRequirementCode().setPassed(true);
        requirementsMet.add(new GradRequirement(pr.getOptionalProgramRequirementCode().getTraxReqNumber(), pr.getOptionalProgramRequirementCode().getLabel(),pr.getOptionalProgramRequirementCode().getOptProReqCode()));

    }

    public static void setGradReqMet(StudentCourse sc, ProgramRequirement gradProgramRule) {
        if(gradProgramRule.getProgramRequirementCode().getTraxReqNumber() != null) {
            if (sc.getGradReqMet().length() > 0) {
                sc.setGradReqMet(sc.getGradReqMet() + ", " + gradProgramRule.getProgramRequirementCode().getTraxReqNumber());
                sc.setGradReqMetDetail(sc.getGradReqMetDetail() + ", " + gradProgramRule.getProgramRequirementCode().getTraxReqNumber() + " - "
                        + gradProgramRule.getProgramRequirementCode().getLabel());
            } else {
                sc.setGradReqMet(gradProgramRule.getProgramRequirementCode().getTraxReqNumber());
                sc.setGradReqMetDetail(
                        gradProgramRule.getProgramRequirementCode().getTraxReqNumber() + " - " + gradProgramRule.getProgramRequirementCode().getLabel());
            }
        }
    }
    public static int processExtraCredits(boolean extraCreditsUsed, int extraCreditsLDcrses, StudentCourse sc, int totalCredits, int requiredCredits) {
        if (extraCreditsUsed && extraCreditsLDcrses != 0) {
            if (totalCredits + extraCreditsLDcrses <= requiredCredits) {
                totalCredits += extraCreditsLDcrses;
                sc.setCreditsUsedForGrad(extraCreditsLDcrses);
            } else {
                int extraCredits = totalCredits + extraCreditsLDcrses - requiredCredits;
                totalCredits = requiredCredits;
                sc.setCreditsUsedForGrad(extraCreditsLDcrses - extraCredits);
            }
        } else {
            if (totalCredits + sc.getCredits() <= requiredCredits) {
                totalCredits += sc.getCredits();
                sc.setCreditsUsedForGrad(sc.getCredits());
            } else {
                int extraCredits = totalCredits + sc.getCredits() - requiredCredits;
                totalCredits = requiredCredits;
                sc.setCreditsUsedForGrad(sc.getCredits() - extraCredits);
            }
        }
        return totalCredits;
    }
    public static void checkCredits(int totalCredits, int requiredCredits, ProgramRequirement gradProgramRule, RuleProcessorData ruleProcessorData) {
        if (totalCredits >= requiredCredits) {
            logger.debug("{} Passed",gradProgramRule.getProgramRequirementCode().getLabel());
            gradProgramRule.getProgramRequirementCode().setPassed(true);

            List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMet();

            if (reqsMet == null)
                reqsMet = new ArrayList<>();

            reqsMet.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getTraxReqNumber(), gradProgramRule.getProgramRequirementCode().getLabel(),gradProgramRule.getProgramRequirementCode().getProReqCode()));
            ruleProcessorData.setRequirementsMet(reqsMet);
            logger.debug("Min Elective Credits Rule: Total-{} Required- {}",totalCredits,requiredCredits);

        } else {
            logger.debug("{} Failed!",gradProgramRule.getProgramRequirementCode().getDescription());
            ruleProcessorData.setGraduated(false);

            List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasons();

            if (nonGradReasons == null)
                nonGradReasons = new ArrayList<>();

            nonGradReasons.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getTraxReqNumber(), gradProgramRule.getProgramRequirementCode().getNotMetDesc(),gradProgramRule.getProgramRequirementCode().getProReqCode()));
            ruleProcessorData.setNonGradReasons(nonGradReasons);
        }
    }

    public static void checkCredits1996(int totalCredits, int requiredCredits, ProgramRequirement gradProgramRule, RuleProcessorData ruleProcessorData) {

        List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMet();
        List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasons();
        GradRequirement gr = new GradRequirement(gradProgramRule.getProgramRequirementCode().getTraxReqNumber(),
                gradProgramRule.getProgramRequirementCode().getLabel(),gradProgramRule.getProgramRequirementCode().getProReqCode());

        if (totalCredits >= requiredCredits) {
            logger.debug("{} Passed",gradProgramRule.getProgramRequirementCode().getLabel());
            gradProgramRule.getProgramRequirementCode().setPassed(true);

            if (reqsMet == null)
                reqsMet = new ArrayList<>();
            if (nonGradReasons == null)
                nonGradReasons = new ArrayList<>();

            if (!reqsMet.contains(gr)) {
                reqsMet.add(gr);
                ruleProcessorData.setRequirementsMet(reqsMet);
                logger.debug("Min Elective Credits Rule: Total-{} Required- {}",totalCredits,requiredCredits);
            }

            //When you add the requirement to ReqMet List, remove them from the NotGradReasons list if they exist
            nonGradReasons.remove(gr);
            ruleProcessorData.setNonGradReasons(nonGradReasons);

        } else {
            logger.debug("{} Failed!",gradProgramRule.getProgramRequirementCode().getDescription());
            ruleProcessorData.setGraduated(false);

            if (nonGradReasons == null)
                nonGradReasons = new ArrayList<>();

            if (!nonGradReasons.contains(gr)) {
                nonGradReasons.add(gr);
                ruleProcessorData.setNonGradReasons(nonGradReasons);
            }
        }
    }

    public static void copyAndAddIntoProgramRulesList(ProgramRequirement programRule, List<ProgramRequirement> finalProgramRulesList, ObjectMapper objectMapper) {
        try {
            ProgramRequirement tempPR = objectMapper.readValue(objectMapper.writeValueAsString(programRule), ProgramRequirement.class);
            if (tempPR != null && !finalProgramRulesList.contains(tempPR)) {
                finalProgramRulesList.add(tempPR);

                //See if there are duplicates
                List<ProgramRequirement> duplicateProgramRules = finalProgramRulesList.stream()
                        .filter(fprl -> fprl.getProgramRequirementCode().getProReqCode().compareTo(tempPR.getProgramRequirementCode().getProReqCode()) == 0)
                        .toList();

                if (duplicateProgramRules.size() > 1) {
                    finalProgramRulesList.removeAll(
                            duplicateProgramRules.stream().filter(dpr -> !dpr.getProgramRequirementCode().isPassed()).toList()
                    );
                }
            }
            logger.debug("TempPR: {}",tempPR);
            logger.debug("Final Program rules list size: {}",finalProgramRulesList.size());
        } catch (IOException e) {
            logger.error(ERROR_FORMAT_STR,e.getMessage());
        }
    }

    public static void copyAndAddIntoOptionalProgramRulesList(OptionalProgramRequirement optionalProgramRule, List<OptionalProgramRequirement> finalOptionalProgramRulesList, ObjectMapper objectMapper) {
        try {
            OptionalProgramRequirement tempSPR = objectMapper.readValue(objectMapper.writeValueAsString(optionalProgramRule),
                    OptionalProgramRequirement.class);
            if (tempSPR != null && !finalOptionalProgramRulesList.contains(optionalProgramRule)) {
                //If Rule already exists in the list then remove and replace
                OptionalProgramRequirement opr = finalOptionalProgramRulesList.stream()
                        .filter(pr -> pr.getOptionalProgramRequirementID().compareTo(tempSPR.getOptionalProgramRequirementID()) == 0)
                                .findAny().orElse(null);

                if (opr != null) {
                    // If Rule already added before, check if the added rule is failed, then replace with current one
                    // Otherwise, do not add anything
                    if (!opr.getOptionalProgramRequirementCode().isPassed()) {
                        finalOptionalProgramRulesList.remove(opr);
                        finalOptionalProgramRulesList.add(tempSPR);
                    }
                }
                // If rule not added yet, just add it
                else {
                    finalOptionalProgramRulesList.add(tempSPR);
                }
            }
            logger.debug("TempPR: {}", tempSPR);
            logger.debug("Final Program rules list size: {}", finalOptionalProgramRulesList.size());
        } catch (IOException e) {
            logger.error(ERROR_FORMAT_STR,e.getMessage());
        }
    }

    public static void copyAndAddIntoStudentCoursesList(StudentCourse studentCourse, List<StudentCourse> finalCourseList, ObjectMapper objectMapper) {
        try {
            StudentCourse studentCourseTmp = objectMapper.readValue(objectMapper.writeValueAsString(studentCourse), StudentCourse.class);
            if (studentCourseTmp != null) {
                finalCourseList.add(studentCourseTmp);
                logger.debug("Added Student Course: {}", studentCourseTmp);
            }
            logger.debug("Final Student Course List size: {}", finalCourseList.size());
        } catch (IOException e) {
            logger.error(ERROR_FORMAT_STR,e.getMessage());
        }
    }

    public static void copyAndAddIntoStudentAssessmentsList(StudentAssessment studentAssessment, List<StudentAssessment> finalAssessmentList, ObjectMapper objectMapper) {
        try {
            StudentAssessment tempSA = objectMapper.readValue(objectMapper.writeValueAsString(studentAssessment), StudentAssessment.class);
            if (tempSA != null)
                finalAssessmentList.add(tempSA);
            logger.debug("TempSC: {}",tempSA);
            logger.debug("Final Assessment List size: : {}",finalAssessmentList.size());
        } catch (IOException e) {
            logger.error(ERROR_FORMAT_STR,e.getMessage());
        }
    }
}
