package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationsRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(RegistrationsRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {
        List<StudentCourse> studentCourseList = RuleProcessorRuleUtils.getUniqueStudentCourses(ruleProcessorData.getStudentCourses(),ruleProcessorData.isProjected());

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

        logger.debug("Projected Courses (Registrations): {}",(int) studentCourseList.stream().filter(StudentCourse::isProjected).count());

        return ruleProcessorData;
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
    }
}
