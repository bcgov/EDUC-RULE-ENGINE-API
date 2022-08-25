package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.List;


public class ExcludeAfterProgramCutoffRule implements Rule {
    private static Logger logger = LoggerFactory.getLogger(ExcludeAfterProgramCutoffRule.class);

    @Override
    public RuleData fire(RuleProcessorData ruleProcessorData) {

        List<StudentCourse> studentCourseList = RuleProcessorRuleUtils.getUniqueStudentCourses(
                ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());

        logger.debug("###################### Finding Course Taken after program cutoff courses ######################");
        if(ruleProcessorData.getGradProgram().getExpiryDate() == null) {
            return ruleProcessorData;
        }
        String cutoffDate = RuleEngineApiUtils.formatDate(ruleProcessorData.getGradProgram().getExpiryDate(), "yyyy-MM-dd");
        for (StudentCourse studentCourse : studentCourseList) {

            String sessionDate = studentCourse.getSessionDate() + "/01";
            try {
                Date temp = RuleEngineApiUtils.parseDate(sessionDate, "yyyy/MM/dd");
                sessionDate = RuleEngineApiUtils.formatDate(temp, "yyyy-MM-dd");
            } catch (ParseException pe) {
                logger.error("ERROR: {}",pe.getMessage());
            }

            int diff = RuleEngineApiUtils.getDifferenceInMonths(sessionDate,cutoffDate);

            if (diff < 0) {
                studentCourse.setCutOffCourse(true);
            }
        }

        ruleProcessorData.setExcludedCourses(RuleProcessorRuleUtils.maintainExcludedCourses(studentCourseList,ruleProcessorData.getExcludedCourses(),ruleProcessorData.isProjected()));
        ruleProcessorData.setStudentCourses(studentCourseList);

        logger.info("Cut-off Courses: {}",
                (int) studentCourseList
                        .stream()
                        .filter(StudentCourse::isCutOffCourse)
                        .count());

        return ruleProcessorData;
    }

}
