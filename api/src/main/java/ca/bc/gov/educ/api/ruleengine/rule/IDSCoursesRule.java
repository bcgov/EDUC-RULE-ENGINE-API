package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class IDSCoursesRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(IDSCoursesRule.class);

    @Override
    public RuleData fire(RuleProcessorData ruleProcessorData) {

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
