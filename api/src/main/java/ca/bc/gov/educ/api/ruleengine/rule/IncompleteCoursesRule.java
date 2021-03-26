package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.struct.RuleData;
import ca.bc.gov.educ.api.ruleengine.struct.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.struct.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class IncompleteCoursesRule implements Rule {
    private static Logger logger = LoggerFactory.getLogger(IncompleteCoursesRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;
    //final RuleType ruleType = RuleType.MATCH;

    public RuleData fire() {

        List<StudentCourse> studentCourseList = new ArrayList<StudentCourse>();
        studentCourseList = ((RuleProcessorData) ruleProcessorData).getStudentCourses();

        logger.debug("###################### Finding INCOMPLETE courses ######################");

        for (StudentCourse studentCourse : studentCourseList) {
            String today = RuleEngineApiUtils.formatDate(new Date(), "yyyy-MM-dd");
            String sessionDate = studentCourse.getSessionDate() + "/01";
            Date temp = new Date();

            try {
                temp = RuleEngineApiUtils.parseDate(sessionDate, "yyyy/MM/dd");
                sessionDate = RuleEngineApiUtils.formatDate(temp, "yyyy-MM-dd");
            } catch (ParseException pe) {
                logger.error("ERROR: " + pe.getMessage());
            }

            int diff = RuleEngineApiUtils.getDifferenceInMonths(today, sessionDate);

            if ("".compareTo(studentCourse.getCompletedCourseLetterGrade().trim()) == 0
                    && diff >= 1) {
                studentCourse.setNotCompleted(true);
            }
        }

        ruleProcessorData.setStudentCourses(studentCourseList);

        logger.info("Not Completed Courses: " +
                (int) studentCourseList
                        .stream()
                        .filter(StudentCourse::isNotCompleted)
                        .count());

        return ruleProcessorData;
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("IncompleteCoursesRule: Rule Processor Data set.");
    }
}
