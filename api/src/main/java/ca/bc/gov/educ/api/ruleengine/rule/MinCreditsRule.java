package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.struct.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class MinCreditsRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(MinCreditsRule.class);

    @Autowired
    private MinCreditRuleData inputData;
    final RuleType ruleType = RuleType.MIN_CREDITS;

    public MinCreditRuleData fire() {
        int totalCredits;
        logger.debug("Min Credits Rule");
        logger.debug("InputData:" + inputData);
        int requiredCredits = Integer.parseInt(inputData.getGradProgramRule().getRequiredCredits().trim());
        StudentCourses studentCourses = inputData.getStudentCourses();
        GradProgramRule programRule = inputData.getGradProgramRule();

        if (studentCourses == null || studentCourses.getStudentCourseList() == null
                || studentCourses.getStudentCourseList().size() == 0) {
            logger.warn("Empty list sent to Min Credits Rule for processing");
            return null;
        }

        if (programRule.getRequiredLevel().trim().compareTo("") == 0) {
            totalCredits = studentCourses.getStudentCourseList()
                    .stream()
                    .mapToInt(studentCourse -> studentCourse.getCreditsUsedForGrad())
                    .sum();
        }
        else {
            totalCredits = studentCourses.getStudentCourseList()
                    .stream()
                    .mapToInt(studentCourse -> studentCourse.getCreditsUsedForGrad())
                    .sum();
        }

        inputData.setRequiredCredits(requiredCredits);
        inputData.setAcquiredCredits(totalCredits);

        if (totalCredits >= requiredCredits)
            inputData.setPassed(true);

        logger.debug("Min Credits -> Required:" + requiredCredits + " Has:" + totalCredits);
        //return totalCredits >= requiredCredits;
        return inputData;
    }

}
