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
public class IDSCoursesRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(IDSCoursesRule.class);

    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {

        List<StudentCourse> studentCourseList = RuleProcessorRuleUtils.getUniqueStudentCourses(ruleProcessorData.getStudentCourses(),ruleProcessorData.isProjected());

        logger.debug("###################### Finding Independent Directed Studies (IDS)  courses ######################");

        for (StudentCourse studentCourse : studentCourseList) {
            if (studentCourse.getCourseCode().startsWith("IDS")) {
                studentCourse.setIndependentDirectedStudies(true);
            }
        }

        ruleProcessorData.setExcludedCourses(RuleProcessorRuleUtils.maintainExcludedCourses(studentCourseList,ruleProcessorData.getExcludedCourses(),ruleProcessorData.isProjected()));
        ruleProcessorData.setStudentCourses(studentCourseList);

        logger.info("Independent Directed Studies Courses: {}", (int) studentCourseList.stream().filter(StudentCourse::isIndependentDirectedStudies).count());

        return ruleProcessorData;
    }

}
