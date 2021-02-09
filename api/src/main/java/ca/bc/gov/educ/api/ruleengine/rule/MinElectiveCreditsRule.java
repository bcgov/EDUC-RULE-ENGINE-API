package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.struct.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class MinElectiveCreditsRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(MinElectiveCreditsRule.class);

    @Autowired
    private MinElectiveCreditRuleData inputData;
    final RuleType ruleType = RuleType.MIN_CREDITS_ELECTIVE;

    public MinElectiveCreditRuleData fire() {
        int totalCredits;
        logger.debug("Min Elective Credits Rule");
        logger.debug("InputData:" + inputData);
        int requiredCredits = Integer.parseInt(inputData.getGradProgramRule().getRequiredCredits().trim());
        StudentCourses studentCourses = inputData.getStudentCourses();
        GradProgramRule programRule = inputData.getGradProgramRule();

        if (studentCourses == null || studentCourses.getStudentCourseList() == null
                || studentCourses.getStudentCourseList().size() == 0) {
            logger.warn("Empty list sent to Min Elective Credits Rule for processing");
            return null;
        }

        if (programRule.getRequiredLevel() != null
                && programRule.getRequiredLevel().trim().compareTo("") == 0) {
            totalCredits = studentCourses.getStudentCourseList()
                    .stream()
                    .filter(sc -> !sc.isUsed())
                    .collect(Collectors.toList())
                    .stream()
                    .mapToInt(studentCourse -> studentCourse.getCreditsUsedForGrad())
                    .sum();
        }
        else {
            totalCredits = studentCourses.getStudentCourseList()
                    .stream()
                    .filter(sc -> !sc.isUsed())
                    .collect(Collectors.toList())
                    .stream()
                    .mapToInt(studentCourse -> studentCourse.getCreditsUsedForGrad())
                    .sum();
        }

        inputData.setRequiredCredits(requiredCredits);
        inputData.setAcquiredCredits(totalCredits);

        if (totalCredits >= requiredCredits)
            inputData.setPassed(true);

        logger.debug("Min Elective Credits -> Required:" + requiredCredits + " Has:" + totalCredits);
        //return totalCredits >= requiredCredits;
        return inputData;
    }

   /* public MinElectiveCreditRuleData fire() {
        int totalCredits;
        int requiredCredits = Integer.parseInt(inputData.getProgramRule().getRequiredCredits().trim());
        StudentCourses studentCourses = inputData.getStudentCourses();
        ProgramRule programRule = inputData.getProgramRule();

        if (studentCourses == null || studentCourses.getStudentCourseList() == null
                || studentCourses.getStudentCourseList().size() == 0)
            return null;

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

        logger.debug("Min Elective Credits -> Required:" + requiredCredits + " Has:" + totalCredits);
        //return totalCredits >= requiredCredits;
        return new MinElectiveCreditRuleData();
    }*/

}
