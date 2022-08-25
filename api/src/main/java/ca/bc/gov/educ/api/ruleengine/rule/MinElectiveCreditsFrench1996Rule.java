package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class MinElectiveCreditsFrench1996Rule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(MinElectiveCreditsFrench1996Rule.class);

    @Override
    public RuleData fire(RuleProcessorData ruleProcessorData) {

        int totalCredits = 0;
        int requiredCredits;
        logger.debug("Min Elective Credits French 1996 Rule");

        if (ruleProcessorData.getStudentCourses().isEmpty()) {
            logger.warn("!!!Empty list sent to Min Elective Credits Rule for processing");
            return ruleProcessorData;
        }

        List<StudentCourse> studentCourses = RuleProcessorRuleUtils
                .getUniqueStudentCourses(ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());

        logger.debug("Unique Courses: {}",studentCourses.size());

        List<ProgramRequirement> gradProgramRules = ruleProcessorData
                .getGradProgramRules().stream().filter(gpr -> "MCE".compareTo(gpr.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
                        && "Y".compareTo(gpr.getProgramRequirementCode().getActiveRequirement()) == 0 && "C".compareTo(gpr.getProgramRequirementCode().getRequirementCategory()) == 0)
                .collect(Collectors.toList());

        for (ProgramRequirement gradProgramRule : gradProgramRules) {
           if(gradProgramRule.getProgramRequirementCode().getRequiredLevel() != null && gradProgramRule.getProgramRequirementCode().getRequiredLevel().trim().compareTo("11 or 12") == 0 ) {
               requiredCredits = Integer.parseInt(gradProgramRule.getProgramRequirementCode().getRequiredCredits().trim()); // list

               List<StudentCourse> tempStudentCourseList;
               tempStudentCourseList = studentCourses.stream()
                           .filter(sc -> !sc.isUsedInMatchRule()
                                   && (sc.getCourseLevel().contains("11")
                                   || sc.getCourseLevel().contains("12"))
                                   && sc.getLanguage() != null && sc.getLanguage().trim().compareTo("F")==0)
                           .collect(Collectors.toList());


               for (StudentCourse sc : tempStudentCourseList) {
                   if (totalCredits + sc.getCredits() <= requiredCredits) {
                       totalCredits += sc.getCredits();
                       sc.setCreditsUsedForGrad(sc.getCredits());
                   } else {
                       int extraCredits = totalCredits + sc.getCredits() - requiredCredits;
                       totalCredits = requiredCredits;
                       sc.setCreditsUsedForGrad(sc.getCredits() - extraCredits);
                   }
                   AlgorithmSupportRule.setGradReqMet(sc,gradProgramRule);
                   sc.setUsed(true);

                   if (totalCredits == requiredCredits) {
                       break;
                   }

               }

               if (totalCredits >= requiredCredits) {
                   logger.debug("{} Passed",gradProgramRule.getProgramRequirementCode().getLabel());
                   gradProgramRule.getProgramRequirementCode().setPassed(true);

                   List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMet();

                   if (reqsMet == null)
                       reqsMet = new ArrayList<>();

                   reqsMet.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getTraxReqNumber(), gradProgramRule.getProgramRequirementCode().getLabel(),gradProgramRule.getProgramRequirementCode().getProReqCode()));
                   ruleProcessorData.setRequirementsMet(reqsMet);
                   logger.debug("Min Credits Elective 12 Rule: Total-{} Requried: {}",totalCredits,requiredCredits);

               } else {
                   logger.debug("{} Failed!",gradProgramRule.getProgramRequirementCode().getDescription());
                   ruleProcessorData.setGraduated(false);

                   List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasons();

                   if (nonGradReasons == null)
                       nonGradReasons = new ArrayList<>();

                   nonGradReasons.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getTraxReqNumber(), gradProgramRule.getProgramRequirementCode().getNotMetDesc(),gradProgramRule.getProgramRequirementCode().getProReqCode()));
                   ruleProcessorData.setNonGradReasons(nonGradReasons);
               }
               logger.info("Min Elective Credits -> Required:{} Has:{}",requiredCredits,totalCredits);
               totalCredits = 0;
           }
        }
        ruleProcessorData.getStudentCourses().addAll(ruleProcessorData.getExcludedCourses());
        return ruleProcessorData;
    }

}
