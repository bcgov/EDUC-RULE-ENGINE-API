package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class ExcludeValidationCoursesRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(ExcludeValidationCoursesRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {

        List<StudentCourse> studentCourseList = ruleProcessorData.getStudentCourses();

        logger.debug("###################### Finding CAREER PROGRAM courses ######################");

        for (StudentCourse studentCourse : studentCourseList) {
            String sessionDate = studentCourse.getSessionDate() + "/01";
            String cName = studentCourse.getCourseCode()+studentCourse.getCourseLevel();
            if (studentCourse.getProvExamCourse().compareTo("Y")==0 && sessionDate.equalsIgnoreCase("2005/06/01") && (cName.compareTo("SS11") == 0
                    || cName.compareTo("SCH11") == 0
                    || cName.compareTo("FNS12") == 0)) {
                studentCourse.setValidationCourse(true);
            }
        }

        ruleProcessorData.setStudentCourses(studentCourseList);

        logger.info("Validation Courses: {}",
                (int) studentCourseList
                        .stream()
                        .filter(StudentCourse::isValidationCourse)
                        .count());

        return ruleProcessorData;
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("ExcludeValidationCoursesRule: Rule Processor Data set.");
    }
}
