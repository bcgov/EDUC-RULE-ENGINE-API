package ca.bc.gov.educ.api.ruleengine.service;

import ca.bc.gov.educ.api.ruleengine.rule.Rule;
import ca.bc.gov.educ.api.ruleengine.rule.RuleFactory;
import ca.bc.gov.educ.api.ruleengine.rule.RuleType;
import ca.bc.gov.educ.api.ruleengine.struct.MinCreditRuleData;
import ca.bc.gov.educ.api.ruleengine.struct.ProgramRule;
import ca.bc.gov.educ.api.ruleengine.struct.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.struct.StudentCourses;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

import static ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils.parseTraxDate;

@Service
public class RuleEngineService {

    @Value("${endpoint.program-rule.get-program-rules.url}")
    private String getProgramRulesURL;

    @Autowired
    RuleFactory ruleFactory;

    private static Logger logger = LoggerFactory.getLogger(RuleEngineService.class);

    /**
     * Find all the not completed courses
     *
     * @return StudentCourses
     * @throws java.lang.Exception
     */
    public StudentCourses findAllIncompleteCourses(StudentCourses studentCourses) {

        List<StudentCourse> studentCourseList = new ArrayList<StudentCourse>();
        studentCourseList = studentCourses.getStudentCourseList();

        logger.debug("###################### Identifying INCOMPLETE ######################");

        for (StudentCourse studentCourse : studentCourseList) {
            if ("".compareTo(
                    studentCourse.getCompletedCourseLetterGrade().trim()) == 0) {
                studentCourse.setNotCompleted(true);
            }
        }

        studentCourses.setStudentCourseList(studentCourseList);

        return studentCourses;
    }

    /**
     * Find all the failed courses
     *
     * @return StudentCourses
     * @throws java.lang.Exception
     */
    public StudentCourses findAllFailedCourses(StudentCourses studentCourses) {

        List<StudentCourse> studentCourseList = new ArrayList<StudentCourse>();
        studentCourseList = studentCourses.getStudentCourseList();

        logger.debug("###################### Identifying FAILS ######################");

        for (StudentCourse studentCourse : studentCourseList) {
            if ("F".compareTo(studentCourse.getCompletedCourseLetterGrade().trim()) == 0
            || "I".compareTo(studentCourse.getCompletedCourseLetterGrade().trim()) == 0
            || "WR".compareTo(studentCourse.getCompletedCourseLetterGrade().trim()) == 0
            || "NM".compareTo(studentCourse.getCompletedCourseLetterGrade().trim()) == 0) {
                studentCourse.setFailed(true);
            }
        }

        studentCourses.setStudentCourseList(studentCourseList);

        return studentCourses;
    }

    public StudentCourses findAllDuplicateCourses(StudentCourses studentCourses) {

        logger.debug("###################### Identifying Duplicates ######################");

        List<StudentCourse> studentCourseList = new ArrayList<StudentCourse>();
        studentCourseList = studentCourses.getStudentCourseList();

        for (int i=0; i < studentCourseList.size()-1; i++) {

            for (int j=i+1; j<studentCourseList.size(); j++) {

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
                    }else if (studentCourseList.get(i).getCompletedCoursePercentage() < studentCourseList.get(j).getCompletedCoursePercentage()) {
                        //courseAchievements.set(i, courseAchievements.get(j));
                        studentCourseList.get(i).setDuplicate(true);
                        studentCourseList.get(j).setDuplicate(false);
                    }else if (studentCourseList.get(i).getCompletedCoursePercentage() == studentCourseList.get(j).getCompletedCoursePercentage()) {

                        if (parseTraxDate(studentCourseList.get(i).getSessionDate())
                                .compareTo(parseTraxDate(studentCourseList.get(j).getSessionDate())) < 0) {
                            //courseAchievements.set(j, courseAchievements.get(i));
                            studentCourseList.get(i).setDuplicate(false);
                            studentCourseList.get(j).setDuplicate(true);
                        }else if (parseTraxDate(studentCourseList.get(i).getSessionDate())
                                .compareTo(parseTraxDate(studentCourseList.get(j).getSessionDate())) >= 0) {
                            //courseAchievements.set(i, courseAchievements.get(j));
                            studentCourseList.get(i).setDuplicate(true);
                            studentCourseList.get(j).setDuplicate(false);
                        }
                    }
                }
                else {
                    //Do Nothing
                }
            }
        }

        //Remove duplicates
        //copy = copy.stream().distinct().collect(Collectors.toList());

        return studentCourses;
    }

    public boolean runMinCreditsRule(MinCreditRuleData minCreditRuleData) {

        String ruleType = minCreditRuleData.getProgramRule().getRequirementType();
        Rule rule = ruleFactory.createRule(RuleType.valueOf(ruleType));
        boolean result = ruleFactory.createRuleEngine(rule).fireRules();

        return result;
    }

    public boolean runMatchCreditsRule(MinCreditRuleData minCreditRuleData) {

        String ruleType = minCreditRuleData.getProgramRule().getRequirementType();
        Rule rule = ruleFactory.createRule(RuleType.valueOf(ruleType));
        boolean result = ruleFactory.createRuleEngine(rule).fireRules();

        return result;
    }

    public boolean runMinElectiveCreditsRule(MinCreditRuleData minCreditRuleData) {

        String ruleType = minCreditRuleData.getProgramRule().getRequirementType();
        Rule rule = ruleFactory.createRule(RuleType.valueOf(ruleType));
        boolean result = ruleFactory.createRuleEngine(rule).fireRules();

        return result;
    }

}
