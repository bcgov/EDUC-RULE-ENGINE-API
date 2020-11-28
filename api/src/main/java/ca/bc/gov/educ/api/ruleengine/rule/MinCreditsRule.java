package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.struct.MinCreditRuleData;
import ca.bc.gov.educ.api.ruleengine.struct.ProgramRule;
import ca.bc.gov.educ.api.ruleengine.struct.StudentCourses;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
public class MinCreditsRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(MinCreditsRule.class);

    @Autowired
    private MinCreditRuleData minCreditRuleData;

    public boolean fire(MinCreditRuleData minCreditRuleData) {
        int totalCredits;
        int requiredCredits = Integer.parseInt(minCreditRuleData.getProgramRule().getRequiredCredits().trim());
        StudentCourses studentCourses = minCreditRuleData.getStudentCourses();
        ProgramRule programRule = minCreditRuleData.getProgramRule();

        if (studentCourses == null || studentCourses.getStudentCourseList() == null
                || studentCourses.getStudentCourseList().size() == 0)
            return false;

        if (programRule.getRequiredLevel().trim().compareTo("") == 0) {
            totalCredits = studentCourses.getStudentCourseList()
                    .stream()
                    .filter(studentCourse -> !studentCourse.isDuplicate()
                            && !studentCourse.isFailed()
                    )
                    .mapToInt(studentCourse -> studentCourse.getCredits())
                    .sum();
        }
        else {
            totalCredits = studentCourses.getStudentCourseList()
                    .stream()
                    .filter(studentCourse -> !studentCourse.isDuplicate()
                            && !studentCourse.isFailed()
                            && studentCourse.getCourseLevel().startsWith(programRule.getRequiredLevel() + "")
                            )
                    .mapToInt(studentCourse -> studentCourse.getCredits())
                    .sum();
        }

        logger.debug("Min Credits -> Required:" + requiredCredits + " Has:" + totalCredits);
        return totalCredits >= requiredCredits;
    }

    @Override
    public <T> boolean fire(T parameters) {
        return false;
    }
}
