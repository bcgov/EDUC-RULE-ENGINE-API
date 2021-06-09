package ca.bc.gov.educ.api.ruleengine.rule;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class AdultCPCoursesRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(AdultCPCoursesRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {

         List<StudentCourse> studentCourseList = ruleProcessorData.getStudentCourses();

        logger.debug("###################### Finding CAREER PROGRAM courses ######################");

        for (StudentCourse studentCourse : studentCourseList) {
            if (studentCourse.getCourseCode().startsWith("CP") 
            		&& (RuleEngineApiUtils.parsingTraxDate(studentCourse.getSessionDate()).compareTo(RuleEngineApiUtils.parsingTraxDate("2000/09")) > 0) 
            		&& !studentCourse.getCourseCode().equalsIgnoreCase("CPWE") 
            		&& studentCourse.getCourseLevel().equalsIgnoreCase("12")) {
                studentCourse.setCareerPrep(true);
            }
        }

        ruleProcessorData.setStudentCourses(studentCourseList);

        logger.info("Career Program Courses: " +
                (int) studentCourseList
                        .stream()
                        .filter(StudentCourse::isCareerPrep)
                        .count());

        return ruleProcessorData;
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("AdultCPCoursesRule: Rule Processor Data set.");
    }
}
