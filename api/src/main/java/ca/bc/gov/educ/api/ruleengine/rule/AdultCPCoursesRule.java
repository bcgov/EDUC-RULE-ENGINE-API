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

import java.util.List;

@Data
@AllArgsConstructor
public class AdultCPCoursesRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(AdultCPCoursesRule.class);

    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {

        List<StudentCourse> studentCourseList = RuleProcessorRuleUtils.getUniqueStudentCourses(
                ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());

        logger.debug("###################### Finding CAREER PROGRAM courses ######################");

        for (StudentCourse studentCourse : studentCourseList) {
            boolean isCPWEExceptionCourse = studentCourse.getCourseCode().equalsIgnoreCase("CPWE") && studentCourse.getCourseLevel().equalsIgnoreCase("12");
            if (studentCourse.getCourseCode().startsWith("CP")
            		&& (RuleEngineApiUtils.parsingTraxDate(studentCourse.getSessionDate()).compareTo(RuleEngineApiUtils.parsingTraxDate("2000/09")) > 0) 
            		&& !isCPWEExceptionCourse) {
                studentCourse.setCareerPrep(true);
            }
        }

        ruleProcessorData.setExcludedCourses(RuleProcessorRuleUtils.maintainExcludedCourses(studentCourseList,ruleProcessorData.getExcludedCourses(),ruleProcessorData.isProjected()));
        ruleProcessorData.setStudentCourses(studentCourseList);

        logger.info("Career Program Courses: {}",(int) studentCourseList.stream().filter(StudentCourse::isCareerPrep).count());

        return ruleProcessorData;
    }

}
