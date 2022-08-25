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


public class RegistrationsRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(RegistrationsRule.class);

    @Override
    public RuleData fire(RuleProcessorData ruleProcessorData) {
        List<StudentCourse> studentCourseList = RuleProcessorRuleUtils.getUniqueStudentCourses(ruleProcessorData.getStudentCourses(),ruleProcessorData.isProjected());

        logger.debug("###################### Finding PROJECTED courses (For Projected GRAD) ######################");

        for (StudentCourse studentCourse : studentCourseList) {
            String today = RuleEngineApiUtils.formatDate(new Date(), "yyyy-MM-dd");
            String sessionDate = studentCourse.getSessionDate() + "/01";

            try {
                Date temp = RuleEngineApiUtils.parseDate(sessionDate, "yyyy/MM/dd");
                sessionDate = RuleEngineApiUtils.formatDate(temp, "yyyy-MM-dd");
            } catch (ParseException pe) {
                logger.error("ERROR: {}",pe.getMessage());
            }

            int diff = RuleEngineApiUtils.getDifferenceInMonths(sessionDate,today);

            String completedCourseLetterGrade = "";
            if(studentCourse.getCompletedCourseLetterGrade() != null) {
            	completedCourseLetterGrade = studentCourse.getCompletedCourseLetterGrade();
            }
            if ("".compareTo(completedCourseLetterGrade.trim()) == 0
                    && diff <= 0) {
                studentCourse.setProjected(true);
            }
        }

        ruleProcessorData.setStudentCourses(studentCourseList);

        logger.info("Projected Courses (Registrations): {}",(int) studentCourseList.stream().filter(StudentCourse::isProjected).count());

        return ruleProcessorData;
    }
}
