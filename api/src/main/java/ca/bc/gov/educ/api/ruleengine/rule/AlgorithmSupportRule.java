package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AlgorithmSupportRule {
    private static final Logger logger = LoggerFactory.getLogger(AlgorithmSupportRule.class);

    private AlgorithmSupportRule() {
    }

    public static void processEmptyCourseCondition(RuleProcessorData ruleProcessorData,List<ProgramRequirement> gradProgramRulesMatch, List<GradRequirement> requirementsNotMet) {
        List<StudentCourse> courseList = RuleProcessorRuleUtils.getUniqueStudentCourses(
                ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());
        ruleProcessorData.setStudentCourses(courseList);
        List<ProgramRequirement> failedRules = gradProgramRulesMatch.stream()
                .filter(pr -> !pr.getProgramRequirementCode().isPassed() && pr.getProgramRequirementCode().getRequirementCategory().equalsIgnoreCase("C")).collect(Collectors.toList());

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
        List<StudentAssessment> finalAssessmentList = new ArrayList<>();
        List<GradRequirement> requirementsMet = new ArrayList<>();
        checkCoursesForEquivalency(gradProgramRulesMatch,courseList,finalAssessmentList,ruleProcessorData,requirementsMet);
        ruleProcessorData.setStudentAssessments(finalAssessmentList);
        ruleProcessorData.setStudentCourses(courseList);
        List<ProgramRequirement> failedRules = gradProgramRulesMatch.stream()
                .filter(pr -> !pr.getProgramRequirementCode().isPassed() && pr.getProgramRequirementCode().getRequirementCategory().equalsIgnoreCase("A")).collect(Collectors.toList());

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

        List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMet();

        if (reqsMet == null)
            reqsMet = new ArrayList<>();

        reqsMet.addAll(requirementsMet);
        ruleProcessorData.setRequirementsMet(reqsMet);
        if(ruleProcessorData.getStudentAssessments() == null || ruleProcessorData.getStudentAssessments().isEmpty()) {
            ruleProcessorData.setStudentAssessments(ruleProcessorData.getExcludedAssessments());
        }else {
            ruleProcessorData.getStudentAssessments().addAll(ruleProcessorData.getExcludedAssessments());
        }
    }

    public static void checkCoursesForEquivalency(List<ProgramRequirement> finalProgramRulesList, List<StudentCourse> courseList, List<StudentAssessment> finalAssessmentList, RuleProcessorData ruleProcessorData, List<GradRequirement> requirementsMet) {
        for(ProgramRequirement pr:finalProgramRulesList) {
            ruleFor116(pr,courseList,finalAssessmentList,ruleProcessorData,requirementsMet);
            ruleFor115(pr,courseList,finalAssessmentList,ruleProcessorData,requirementsMet);
            ruleFor118(pr,courseList,finalAssessmentList,ruleProcessorData,requirementsMet);
            ruleFor404(pr,courseList,finalAssessmentList,ruleProcessorData,requirementsMet);
        }
    }

    private static void ruleFor404(ProgramRequirement pr, List<StudentCourse> courseList, List<StudentAssessment> finalAssessmentList, RuleProcessorData ruleProcessorData, List<GradRequirement> requirementsMet) {
        if(!pr.getProgramRequirementCode().isPassed() && pr.getProgramRequirementCode().getProReqCode().compareTo("404")==0) {
            for(StudentCourse sc:courseList) {
                if(sc.getMetLitNumRequirement() != null && (sc.getMetLitNumRequirement().equalsIgnoreCase("LTE12"))) {
                    createAssessmentRecord(finalAssessmentList,sc.getMetLitNumRequirement(),ruleProcessorData.getAssessmentList(),pr,ruleProcessorData.getGradStudent().getPen(),requirementsMet);
                }
            }
        }
    }

    private static void ruleFor118(ProgramRequirement pr, List<StudentCourse> courseList, List<StudentAssessment> finalAssessmentList, RuleProcessorData ruleProcessorData, List<GradRequirement> requirementsMet) {
        if(!pr.getProgramRequirementCode().isPassed() && pr.getProgramRequirementCode().getProReqCode().compareTo("118")==0) {
            for(StudentCourse sc:courseList) {
                if(sc.getMetLitNumRequirement() != null && (sc.getMetLitNumRequirement().equalsIgnoreCase("LTE12") ||
                        sc.getMetLitNumRequirement().equalsIgnoreCase("LTP12"))) {
                    createAssessmentRecord(finalAssessmentList,sc.getMetLitNumRequirement(),ruleProcessorData.getAssessmentList(),pr,ruleProcessorData.getGradStudent().getPen(),requirementsMet);
                }
            }
        }
    }

    private static void ruleFor115(ProgramRequirement pr, List<StudentCourse> courseList, List<StudentAssessment> finalAssessmentList, RuleProcessorData ruleProcessorData, List<GradRequirement> requirementsMet) {
        if(!pr.getProgramRequirementCode().isPassed() && pr.getProgramRequirementCode().getProReqCode().compareTo("115")==0) {
            for(StudentCourse sc:courseList) {
                if(sc.getMetLitNumRequirement() != null && (sc.getMetLitNumRequirement().equalsIgnoreCase("LTE10") ||
                        sc.getMetLitNumRequirement().equalsIgnoreCase("LTP10"))) {
                    createAssessmentRecord(finalAssessmentList,sc.getMetLitNumRequirement(),ruleProcessorData.getAssessmentList(),pr,ruleProcessorData.getGradStudent().getPen(),requirementsMet);
                }
            }
        }
    }
    private static void ruleFor116(ProgramRequirement pr, List<StudentCourse> courseList, List<StudentAssessment> finalAssessmentList, RuleProcessorData ruleProcessorData, List<GradRequirement> requirementsMet) {
        if(!pr.getProgramRequirementCode().isPassed() && pr.getProgramRequirementCode().getProReqCode().compareTo("116")==0) {
            for(StudentCourse sc:courseList) {
                if(sc.getMetLitNumRequirement() != null && (sc.getMetLitNumRequirement().equalsIgnoreCase("NME10") ||
                        sc.getMetLitNumRequirement().equalsIgnoreCase("NME") ||
                        sc.getMetLitNumRequirement().equalsIgnoreCase("NMF10") ||
                        sc.getMetLitNumRequirement().equalsIgnoreCase("NMF"))) {
                    createAssessmentRecord(finalAssessmentList,sc.getMetLitNumRequirement(),ruleProcessorData.getAssessmentList(),pr,ruleProcessorData.getGradStudent().getPen(),requirementsMet);
                }
            }
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
}
