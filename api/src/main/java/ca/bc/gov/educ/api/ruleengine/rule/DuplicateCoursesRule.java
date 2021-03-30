package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.struct.RuleData;
import ca.bc.gov.educ.api.ruleengine.struct.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.struct.StudentCourse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class DuplicateCoursesRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(DuplicateCoursesRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {

        logger.debug("###################### Finding DUPLICATE courses ######################");

        List<StudentCourse> studentCourseList = new ArrayList<StudentCourse>();
        studentCourseList = ruleProcessorData.getStudentCourses();

        for (int i = 0; i < studentCourseList.size() - 1; i++) {

            for (int j = i + 1; j < studentCourseList.size(); j++) {

                if (studentCourseList.get(i).getCourseCode().equals(studentCourseList.get(j).getCourseCode())
                        && studentCourseList.get(i).getCourseLevel().equals(studentCourseList.get(j).getCourseLevel())) {

                    logger.debug("comparing " + studentCourseList.get(i).getCourseCode() + " with "
                            + studentCourseList.get(j).getCourseCode() + " -> Duplicate FOUND - CourseID:"
                            + studentCourseList.get(i).getCourseCode() + "-" + studentCourseList.get(i).getCourseLevel());

                    //      IF finalPercent of A greater than finalPercent of B -> SELECT A copy to B
                    //      IF finalPercent of B greater than finalPercent of A -> SELECT B copy to A
                    //      IF finalPercent of A equals to finalPercent of B ->
                    //              IF sessionDate of A is older than sessionDate of B -> SELECT A copy to B
                    //              IF sessionDate of B is older than sessionDate of A -> SELECT B copy  to A
                    //              IF sessionDate of A is equal to sessionDate of B -> SELECT A copy to B

                    if (studentCourseList.get(i).getCompletedCoursePercentage() > studentCourseList.get(j).getCompletedCoursePercentage()) {
                        //copy.set(j, copy.get(i));
                        studentCourseList.get(i).setDuplicate(false);
                        studentCourseList.get(j).setDuplicate(true);
                    } else if (studentCourseList.get(i).getCompletedCoursePercentage() < studentCourseList.get(j).getCompletedCoursePercentage()) {
                        //courseAchievements.set(i, courseAchievements.get(j));
                        studentCourseList.get(i).setDuplicate(true);
                        studentCourseList.get(j).setDuplicate(false);
                    } else if (studentCourseList.get(i).getCompletedCoursePercentage() == studentCourseList.get(j).getCompletedCoursePercentage()) {

                        if (RuleEngineApiUtils.parseTraxDate(studentCourseList.get(i).getSessionDate())
                                .compareTo(RuleEngineApiUtils.parseTraxDate(studentCourseList.get(j).getSessionDate())) < 0) {
                            //courseAchievements.set(j, courseAchievements.get(i));
                            studentCourseList.get(i).setDuplicate(false);
                            studentCourseList.get(j).setDuplicate(true);
                        } else if (RuleEngineApiUtils.parseTraxDate(studentCourseList.get(i).getSessionDate())
                                .compareTo(RuleEngineApiUtils.parseTraxDate(studentCourseList.get(j).getSessionDate())) >= 0) {
                            //courseAchievements.set(i, courseAchievements.get(j));
                            studentCourseList.get(i).setDuplicate(true);
                            studentCourseList.get(j).setDuplicate(false);
                        }
                    }
                } else {
                    //Do Nothing
                }
            }
        }

        logger.info("Duplicate Courses: " +
                (int) studentCourseList
                        .stream()
                        .filter(StudentCourse::isDuplicate)
                        .count());

        return ruleProcessorData;
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("DuplicateCoursesRule: Rule Processor Data set.");
    }
}
