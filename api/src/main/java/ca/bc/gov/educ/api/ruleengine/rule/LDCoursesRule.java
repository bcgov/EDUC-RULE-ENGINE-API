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
public class LDCoursesRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(LDCoursesRule.class);

    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {

        List<StudentCourse> studentCourseList = RuleProcessorRuleUtils.getUniqueStudentCourses(ruleProcessorData.getStudentCourses(),ruleProcessorData.isProjected());

        logger.debug("###################### Finding LOCALLY DEVELOPED courses ######################");

        for (StudentCourse studentCourse : studentCourseList) {
            if (studentCourse.getCourseCode().startsWith("X"))
                studentCourse.setLocallyDeveloped(true);
        }

        ruleProcessorData.setExcludedCourses(RuleProcessorRuleUtils.maintainExcludedCourses(studentCourseList,ruleProcessorData.getExcludedCourses(),ruleProcessorData.isProjected()));
        ruleProcessorData.setStudentCourses(studentCourseList);

        logger.info("Locally Developed Courses: {}",(int) studentCourseList.stream().filter(StudentCourse::isLocallyDeveloped) .count());

        return ruleProcessorData;
    }

}
