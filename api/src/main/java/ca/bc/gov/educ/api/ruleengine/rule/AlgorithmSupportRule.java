package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AlgorithmSupportRule {
    private static Logger logger = LoggerFactory.getLogger(AlgorithmSupportRule.class);

    private AlgorithmSupportRule() {
    }

    public static void processEmptyAssessmentCourseCondition(RuleProcessorData ruleProcessorData,List<ProgramRequirement> gradProgramRulesMatch, List<GradRequirement> requirementsNotMet) {
        List<StudentCourse> courseList = RuleProcessorRuleUtils.getUniqueStudentCourses(
                ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());
        List<StudentAssessment> finalAssessmentList = new ArrayList<>();
        List<GradRequirement> requirementsMet = new ArrayList<>();
        checkCoursesForEquivalency(gradProgramRulesMatch,courseList,finalAssessmentList,ruleProcessorData,requirementsMet);
        ruleProcessorData.setStudentAssessments(finalAssessmentList);
        List<ProgramRequirement> failedRules = gradProgramRulesMatch.stream()
                .filter(pr -> !pr.getProgramRequirementCode().isPassed()).collect(Collectors.toList());

        if (failedRules.isEmpty()) {
            logger.debug("All the match rules met!");
        } else {
            for (ProgramRequirement failedRule : failedRules) {
                requirementsNotMet.add(new GradRequirement(failedRule.getProgramRequirementCode().getTraxReqNumber(), failedRule.getProgramRequirementCode().getNotMetDesc()));
            }

            logger.info("One or more Match rules not met!");
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

        if(ruleProcessorData.getStudentCourses() == null || ruleProcessorData.getStudentCourses().isEmpty()) {
            ruleProcessorData.setStudentCourses(ruleProcessorData.getExcludedCourses());
        }else {
            ruleProcessorData.getStudentCourses().addAll(ruleProcessorData.getExcludedCourses());
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
        requirementsMet.add(new GradRequirement(pr.getProgramRequirementCode().getTraxReqNumber(), pr.getProgramRequirementCode().getLabel()));

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
        requirementsMet.add(new GradRequirement(pr.getOptionalProgramRequirementCode().getTraxReqNumber(), pr.getOptionalProgramRequirementCode().getLabel()));

    }
}
