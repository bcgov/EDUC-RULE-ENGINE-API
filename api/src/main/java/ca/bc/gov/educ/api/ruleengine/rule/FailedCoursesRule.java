package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.struct.RuleData;
import ca.bc.gov.educ.api.ruleengine.struct.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.struct.StudentCourse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class FailedCoursesRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(FailedCoursesRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {

        List<StudentCourse> studentCourseList = new ArrayList<StudentCourse>();
        studentCourseList = ruleProcessorData.getStudentCourses();

        logger.debug("###################### Finding FAILED courses ######################");

        for (StudentCourse studentCourse : studentCourseList) {
            if ("F".compareTo(studentCourse.getCompletedCourseLetterGrade().trim()) == 0
                    || "I".compareTo(studentCourse.getCompletedCourseLetterGrade().trim()) == 0
                    || "WR".compareTo(studentCourse.getCompletedCourseLetterGrade().trim()) == 0
                    || "NM".compareTo(studentCourse.getCompletedCourseLetterGrade().trim()) == 0) {
                studentCourse.setFailed(true);
            }
        }

        ruleProcessorData.setStudentCourses(studentCourseList);

        logger.info("Failed Courses: " +
                (int) studentCourseList
                        .stream()
                        .filter(StudentCourse::isFailed)
                        .count());

        return ruleProcessorData;
    }

    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("FailedCoursesRule: Rule Processor Data set.");
    }
}
