package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiConstants.DATE_FORMAT;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class ExcludeValidationCoursesRule extends BaseRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(ExcludeValidationCoursesRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {

        List<StudentCourse> studentCourseList = RuleProcessorRuleUtils.getUniqueStudentCourses(
                ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());

        for (StudentCourse studentCourse : studentCourseList) {
            String sessionDate = studentCourse.getSessionDate() + "/01";
            try {
                Date temp = toLastDayOfMonth(RuleEngineApiUtils.parseDate(sessionDate, DATE_FORMAT));
                sessionDate = RuleEngineApiUtils.formatDate(temp, DATE_FORMAT);
            } catch (ParseException pe) {
                logger.error("ERROR: {}",pe.getMessage());
            }
            String cName = studentCourse.getCourseCode()+studentCourse.getCourseLevel();
            if (studentCourse.getProvExamCourse().compareTo("Y")==0 && sessionDate.equalsIgnoreCase("2005/06/30") && (cName.compareTo("SS11") == 0
                    || cName.compareTo("SCH11") == 0
                    || cName.compareTo("FNS12") == 0)) {
                studentCourse.setValidationCourse(true);
            }
        }

        ruleProcessorData.setExcludedCourses(RuleProcessorRuleUtils.maintainExcludedCourses("ExcludeValidationCoursesRule", studentCourseList,ruleProcessorData.getExcludedCourses(),ruleProcessorData.isProjected()));
        ruleProcessorData.setStudentCourses(studentCourseList);

        logger.debug("Validation Courses: {}",
                (int) studentCourseList
                        .stream()
                        .filter(StudentCourse::isValidationCourse)
                        .count());

        return ruleProcessorData;
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
    }
}
