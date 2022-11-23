package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class RestrictedCoursesRule implements Rule {

    private static Logger logger = Logger.getLogger(RestrictedCoursesRule.class.getName());

    @Autowired
    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {
        logger.log(Level.INFO, "## Finding COURSE RESTRICTIONS");

        List<StudentCourse> studentCourses = RuleProcessorRuleUtils.getUniqueStudentCourses(
                ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());
        List<CourseRestriction> restrictedCourses = ruleProcessorData.getCourseRestrictions();
        String programCode = ruleProcessorData.getGradProgram().getProgramCode();

        if (restrictedCourses == null) {
            restrictedCourses = new ArrayList<>();
        }

        for (int i = 0; i < studentCourses.size(); i++) {
            StudentCourse sCourse = studentCourses.get(i);
            String courseCode = studentCourses.get(i).getCourseCode();
            String courseLevel = studentCourses.get(i).getCourseLevel();
            String sessionDate = studentCourses.get(i).getSessionDate();

            List<CourseRestriction> shortenedList = getMinimizedRestrictedCourses(programCode, restrictedCourses, courseLevel, courseCode, sessionDate);

            if (!shortenedList.isEmpty()) {
                for (CourseRestriction courseRestriction : shortenedList) {
                    String restrictedCourse = courseRestriction.getRestrictedCourse().trim();
                    String restrictedLevel = courseRestriction.getRestrictedCourseLevel().trim();
                    StudentCourse tempCourseRestriction = studentCourses.stream()
                            .filter(sc -> restrictedCourse.compareTo(sc.getCourseCode()) == 0 && restrictedLevel.compareTo(sc.getCourseLevel()) == 0)
                            .findAny()
                            .orElse(null);
                    if (tempCourseRestriction != null
                            && !tempCourseRestriction.isRestricted()
                            && !sCourse.isRestricted()
                            && (!courseCode.equalsIgnoreCase(restrictedCourse) || (courseCode.equalsIgnoreCase(restrictedCourse) && !courseLevel.equalsIgnoreCase(restrictedLevel)))) {
                        compareCredits(sCourse, tempCourseRestriction, studentCourses, i);
                    }
                }
            }
        }
        ruleProcessorData.setExcludedCourses(RuleProcessorRuleUtils.maintainExcludedCourses(studentCourses, ruleProcessorData.getExcludedCourses(), ruleProcessorData.isProjected()));
        ruleProcessorData.setStudentCourses(studentCourses);
        prepareCoursesForOptionalPrograms();
        logger.log(Level.INFO, "Restricted Courses: {0} ", (int) studentCourses.stream().filter(StudentCourse::isRestricted).count());
        return ruleProcessorData;
    }

    private List<CourseRestriction> getMinimizedRestrictedCourses(String programCode, List<CourseRestriction> restrictedCourses,
                                                                  String courseLevel, String courseCode, String sessionDate) {
        List<CourseRestriction> shortenedList;
        if (StringUtils.isNotBlank(courseLevel)) {
            shortenedList = restrictedCourses.stream()
                    .filter(cR -> courseCode.compareTo(cR.getMainCourse()) == 0
                            && courseLevel.compareTo(cR.getMainCourseLevel()) == 0
                            && is1950AndSameLevel(programCode, cR.getMainCourseLevel(), cR.getRestrictedCourseLevel())
                            && RuleEngineApiUtils.checkDateForRestrictedCourses(cR.getRestrictionStartDate(), cR.getRestrictionEndDate(), sessionDate))
                    .collect(Collectors.toList());
        } else {
            shortenedList = restrictedCourses.stream()
                    .filter(cR -> courseCode.compareTo(cR.getMainCourse()) == 0
                            && is1950AndSameLevel(programCode, cR.getMainCourseLevel(), cR.getRestrictedCourseLevel())
                            && RuleEngineApiUtils.checkDateForRestrictedCourses(cR.getRestrictionStartDate(), cR.getRestrictionEndDate(), sessionDate))
                    .collect(Collectors.toList());
        }
        return shortenedList;
    }

    /*
        For 1950, add course restriction only if they're of the same course level
     */
    private boolean is1950AndSameLevel(String programCode, String courseLevel, String restrictedCourseLevel) {

        if ("1950".compareTo(programCode) == 0) {
            return courseLevel.compareTo(restrictedCourseLevel) == 0;
        }
        return true;
    }

    private void compareCredits(StudentCourse sCourse, StudentCourse tempCourseRestriction, List<StudentCourse> studentCourses, int i) {
        if (sCourse.getCredits().equals(tempCourseRestriction.getCredits())) {
            if (sCourse.getCompletedCoursePercentage().equals(tempCourseRestriction.getCompletedCoursePercentage())) {
                compareSessionDates(sCourse, tempCourseRestriction, studentCourses, i);
            } else
                studentCourses.get(i).setRestricted(sCourse.getCompletedCoursePercentage() <= tempCourseRestriction.getCompletedCoursePercentage());
        } else studentCourses.get(i).setRestricted(sCourse.getCredits() <= tempCourseRestriction.getCredits());
    }

    private void compareSessionDates(StudentCourse sCourse, StudentCourse tempCourseRestriction, List<StudentCourse> studentCourses, int i) {
        if (RuleEngineApiUtils.parsingTraxDate(sCourse.getSessionDate())
                .compareTo(RuleEngineApiUtils.parsingTraxDate(tempCourseRestriction.getSessionDate())) < 0) {
            studentCourses.get(i).setRestricted(false);
        } else if (RuleEngineApiUtils.parsingTraxDate(sCourse.getSessionDate())
                .compareTo(RuleEngineApiUtils.parsingTraxDate(tempCourseRestriction.getSessionDate())) >= 0) {
            studentCourses.get(i).setRestricted(true);
        }
    }

    private void prepareCoursesForOptionalPrograms() {
        List<StudentCourse> listCourses = ruleProcessorData.getStudentCourses();
        Map<String, OptionalProgramRuleProcessor> mapOptional = ruleProcessorData.getMapOptional();
        mapOptional.forEach((k, v) -> v.setStudentCoursesOptionalProgram(RuleEngineApiUtils.getClone(listCourses)));
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("RestrictedCoursesRule: Rule Processor Data set.");
    }
}
