package ca.bc.gov.educ.api.ruleengine.rule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import java.util.List;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class LDCoursesRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(LDCoursesRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {

        List<StudentCourse> studentCourseList = ruleProcessorData.getStudentCourses();

        logger.debug("###################### Finding LOCALLY DEVELOPED courses ######################");

        for (StudentCourse studentCourse : studentCourseList) {
            if (studentCourse.getCourseCode().startsWith("X"))
                studentCourse.setLocallyDeveloped(true);
        }

        ruleProcessorData.setStudentCourses(studentCourseList);

        logger.info("Locally Developed Courses: " +
                (int) studentCourseList
                        .stream()
                        .filter(StudentCourse::isLocallyDeveloped)
                        .count());

        return ruleProcessorData;
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("LDCoursesRule: Rule Processor Data set.");
    }
}
