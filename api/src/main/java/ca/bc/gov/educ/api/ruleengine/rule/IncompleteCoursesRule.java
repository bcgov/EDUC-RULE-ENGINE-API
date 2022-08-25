package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
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
public class IncompleteCoursesRule implements Rule {
    private static Logger logger = LoggerFactory.getLogger(IncompleteCoursesRule.class);

    private RuleProcessorData ruleProcessorData;

    public RuleData fire() {

        List<StudentCourse> studentCourseList = RuleProcessorRuleUtils.getUniqueStudentCourses(ruleProcessorData.getStudentCourses(),ruleProcessorData.isProjected());

        logger.debug("###################### Finding INCOMPLETE courses ######################");

        for (StudentCourse studentCourse : studentCourseList) {
            String today = RuleEngineApiUtils.formatDate(new Date(), "yyyy-MM-dd");
            String sessionDate = studentCourse.getSessionDate() + "/01";

            try {
                Date temp = RuleEngineApiUtils.parseDate(sessionDate, "yyyy/MM/dd");
                sessionDate = RuleEngineApiUtils.formatDate(temp, "yyyy-MM-dd");
            } catch (ParseException pe) {
                logger.error("ERROR: {}" , pe.getMessage());
            }

            int diff = RuleEngineApiUtils.getDifferenceInMonths(sessionDate,today);
            String completedCourseLetterGrade = "";
            if(studentCourse.getCompletedCourseLetterGrade() != null) {
            	completedCourseLetterGrade = studentCourse.getCompletedCourseLetterGrade();
            }
            if ("".compareTo(completedCourseLetterGrade.trim()) == 0
                    && diff > 0) {
                studentCourse.setNotCompleted(true);
            }
        }

        ruleProcessorData.setExcludedCourses(RuleProcessorRuleUtils.maintainExcludedCourses(studentCourseList,ruleProcessorData.getExcludedCourses(),ruleProcessorData.isProjected()));
        ruleProcessorData.setStudentCourses(studentCourseList);
        logger.info("Not Completed Courses: {}",(int) studentCourseList.stream().filter(StudentCourse::isNotCompleted).count());
        return ruleProcessorData;
    }

}
