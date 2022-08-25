package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentAssessment;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
public class IncompleteAssessmentsRule implements Rule {
    private static Logger logger = LoggerFactory.getLogger(IncompleteAssessmentsRule.class);

    private RuleProcessorData ruleProcessorData;

    public RuleData fire() {

        List<StudentAssessment> studentAssessmentList = RuleProcessorRuleUtils.getUniqueStudentAssessments(ruleProcessorData.getStudentAssessments(), ruleProcessorData.isProjected());

        logger.debug("###################### Finding INCOMPLETE Assessments ######################");

        for (StudentAssessment studentAssessment : studentAssessmentList) {
            String today = RuleEngineApiUtils.formatDate(new Date(), "yyyy-MM-dd");
            String sessionDate = studentAssessment.getSessionDate() + "/01";
            try {
                Date temp = RuleEngineApiUtils.parseDate(sessionDate, "yyyy/MM/dd");
                sessionDate = RuleEngineApiUtils.formatDate(temp, "yyyy-MM-dd");
            } catch (ParseException pe) {
                logger.error("ERROR: {}", pe.getMessage());
            }

            int diff = RuleEngineApiUtils.getDifferenceInMonths(sessionDate, today);
            String proficiencyScore;
            if (studentAssessment.getProficiencyScore() == null) {
                proficiencyScore = "0.0";
            } else {
                proficiencyScore = studentAssessment.getProficiencyScore().toString();
            }
            String specialCase;
            if (studentAssessment.getSpecialCase() == null) {
                specialCase = "";
            } else {
                specialCase = studentAssessment.getSpecialCase();
            }
            String exceededWriteFlag;
            if (studentAssessment.getExceededWriteFlag() == null) {
                exceededWriteFlag = "";
            } else {
                exceededWriteFlag = studentAssessment.getExceededWriteFlag();
            }
            if ("".compareTo(specialCase.trim()) == 0
                    && "Y".compareTo(exceededWriteFlag.trim()) != 0
                    && "0.0".compareTo(proficiencyScore) == 0
                    && diff > 0) {
                studentAssessment.setNotCompleted(true);
            }
        }

        ruleProcessorData.setExcludedAssessments(RuleProcessorRuleUtils.maintainExcludedAssessments(studentAssessmentList, ruleProcessorData.getExcludedAssessments(), ruleProcessorData.isProjected()));
        ruleProcessorData.setStudentAssessments(studentAssessmentList);

        logger.info("Not Completed Assessments: {}", (int) studentAssessmentList.stream().filter(StudentAssessment::isNotCompleted).count());

        return ruleProcessorData;
    }

}
