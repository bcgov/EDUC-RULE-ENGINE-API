package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Data
@AllArgsConstructor
public class ExcludeGrade10LevelRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(ExcludeGrade10LevelRule.class);

    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {

        List<StudentCourse> studentCourseList = RuleProcessorRuleUtils.getUniqueStudentCourses(
                ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());

        logger.debug("###################### Finding CAREER PROGRAM courses ######################");

        for (StudentCourse studentCourse : studentCourseList) {
            if (studentCourse.getCourseLevel().compareTo("10")==0) {
                studentCourse.setGrade10Course(true);
            }
        }

        ruleProcessorData.setExcludedCourses(RuleProcessorRuleUtils.maintainExcludedCourses(studentCourseList,ruleProcessorData.getExcludedCourses(),ruleProcessorData.isProjected()));
        ruleProcessorData.setStudentCourses(studentCourseList);

        logger.info("Grade Level 10 Courses: {}",(int) studentCourseList.stream().filter(StudentCourse::isGrade10Course).count());

        return ruleProcessorData;
    }

}
