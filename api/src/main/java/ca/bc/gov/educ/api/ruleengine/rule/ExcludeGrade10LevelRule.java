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
public class ExcludeGrade10LevelRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(ExcludeGrade10LevelRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {

        List<StudentCourse> studentCourseList = ruleProcessorData.getStudentCourses();

        logger.debug("###################### Finding CAREER PROGRAM courses ######################");

        for (StudentCourse studentCourse : studentCourseList) {
            if (studentCourse.getCourseLevel().compareTo("10")==0) {
                studentCourse.setGrade10Course(true);
            }
        }

        ruleProcessorData.setStudentCourses(studentCourseList);

        logger.info("Grade Level 10 Courses: {}",(int) studentCourseList.stream().filter(StudentCourse::isGrade10Course).count());

        return ruleProcessorData;
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("ExcludeGrade10LevelRule: Rule Processor Data set.");
    }
}
