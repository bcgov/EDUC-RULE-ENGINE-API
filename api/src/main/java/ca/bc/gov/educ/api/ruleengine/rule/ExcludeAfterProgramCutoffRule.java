package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentAssessment;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiConstants.DATE_FORMAT;
import static ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiConstants.DEFAULT_DATE_FORMAT;

@EqualsAndHashCode(callSuper = true)
@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class ExcludeAfterProgramCutoffRule extends BaseRule implements Rule {
    private static Logger logger = LoggerFactory.getLogger(ExcludeAfterProgramCutoffRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    public RuleData fire() {
        /* Excluded Courses */
        List<StudentCourse> studentCourseList = RuleProcessorRuleUtils.getUniqueStudentCourses(
                ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());
        if (ruleProcessorData.getGradProgram().getExpiryDate() == null) {
            return ruleProcessorData;
        }
        String cutoffDate = RuleEngineApiUtils.formatDate(ruleProcessorData.getGradProgram().getExpiryDate(), DEFAULT_DATE_FORMAT);

        for (StudentCourse studentCourse : studentCourseList) {
            String sessionDate = studentCourse.getSessionDate() + "/01";
            try {
                Date temp = toLastDayOfMonth(RuleEngineApiUtils.parseDate(sessionDate, DATE_FORMAT));
                sessionDate = RuleEngineApiUtils.formatDate(temp, DEFAULT_DATE_FORMAT);
            } catch (ParseException pe) {
                logger.error("ERROR: {}", pe.getMessage());
            }

            int diff = RuleEngineApiUtils.getDifferenceInMonths(sessionDate, cutoffDate);
            if (diff < 0) {
                studentCourse.setCutOffCourse(true);
            }
        }
        ruleProcessorData.setExcludedCourses(RuleProcessorRuleUtils.maintainExcludedCourses("ExcludeAfterProgramCutoffRule", studentCourseList, ruleProcessorData.getExcludedCourses(), ruleProcessorData.isProjected()));
        ruleProcessorData.setStudentCourses(studentCourseList);

        logger.debug("Cut-off Courses: {}", (int) studentCourseList
                .stream()
                .filter(StudentCourse::isCutOffCourse)
                .count());

        /* Excluded Assessments */
        List<StudentAssessment> studentAssessmentList = RuleProcessorRuleUtils.getUniqueStudentAssessments(
                ruleProcessorData.getStudentAssessments(), ruleProcessorData.isProjected());
        if (ruleProcessorData.getGradProgram().getExpiryDate() == null) {
            return ruleProcessorData;
        }

        for (StudentAssessment studentAssessment : studentAssessmentList) {
            String sessionDate = studentAssessment.getSessionDate() + "/01";
            try {
                Date temp = toLastDayOfMonth(RuleEngineApiUtils.parseDate(sessionDate, DATE_FORMAT));
                sessionDate = RuleEngineApiUtils.formatDate(temp, DEFAULT_DATE_FORMAT);
            } catch (ParseException pe) {
                logger.error("ERROR: {}", pe.getMessage());
            }

            int diff = RuleEngineApiUtils.getDifferenceInMonths(sessionDate, cutoffDate);
            if (diff < 0) {
                studentAssessment.setCutOffAssessment(true);
            }
        }
        ruleProcessorData.setExcludedAssessments(RuleProcessorRuleUtils.maintainExcludedAssessments(studentAssessmentList, ruleProcessorData.getExcludedAssessments(), ruleProcessorData.isProjected()));
        ruleProcessorData.setStudentAssessments(studentAssessmentList);

        logger.debug("Cut-off Assessments: {}", (int) studentAssessmentList
                .stream()
                .filter(StudentAssessment::isCutOffAssessment)
                .count());

        return ruleProcessorData;
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
    }
}
