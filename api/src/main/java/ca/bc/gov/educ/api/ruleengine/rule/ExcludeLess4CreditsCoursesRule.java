package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class ExcludeLess4CreditsCoursesRule implements Rule {

    private static Logger logger = Logger.getLogger(ExcludeLess4CreditsCoursesRule.class.getName());

    @Autowired
    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {

        logger.log(Level.INFO, "###################### Finding 2 Credit Courses ######################");

        List<StudentCourse> studentCourseList = RuleProcessorRuleUtils.getUniqueStudentCourses(
                ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());

        for (StudentCourse studentCourse : studentCourseList) {
            Integer credits = studentCourse.getCredits();
            if(credits < 4) {
                studentCourse.setLessCreditCourse(true);
            }
        }

        ruleProcessorData.setStudentCourses(studentCourseList);

        logger.log(Level.INFO, "Removed 2 Credit Courses: {0} ",
                (int) studentCourseList.stream().filter(StudentCourse::isLessCreditCourse).count());

        return ruleProcessorData;
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("ExcludeLess4CreditsCoursesRule: Rule Processor Data set.");
    }
}
