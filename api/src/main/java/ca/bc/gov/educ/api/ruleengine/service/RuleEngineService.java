package ca.bc.gov.educ.api.ruleengine.service;

import ca.bc.gov.educ.api.ruleengine.rule.*;
import ca.bc.gov.educ.api.ruleengine.struct.*;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
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
        //TODO: Make a new Rule class for Incomplete Courses
        List<StudentCourse> studentCourseList = new ArrayList<StudentCourse>();
        studentCourseList = studentCourses.getStudentCourseList();

        logger.debug("###################### Identifying INCOMPLETE ######################");

        for (StudentCourse studentCourse : studentCourseList) {
            String today = RuleEngineApiUtils.formatDate(new Date(), "yyyy-MM-dd");
            String sessionDate = studentCourse.getSessionDate() + "/01";
            Date temp = new Date();

            try {
                temp = RuleEngineApiUtils.parseDate(sessionDate, "yyyy/MM/dd");
                sessionDate = RuleEngineApiUtils.formatDate(temp, "yyyy-MM-dd");
            }catch (ParseException pe) {
                logger.error("ERROR: " + pe.getMessage());
            }

            int diff = RuleEngineApiUtils.getDifferenceInMonths(today, sessionDate);

            if ("".compareTo(studentCourse.getCompletedCourseLetterGrade().trim()) == 0
                && diff >= 1) {
                studentCourse.setNotCompleted(true);
            }
        }

        studentCourses.setStudentCourseList(studentCourseList);

        return studentCourses;
    }

    /**
     * Find all the registered courses
     *
     * @return StudentCourses
     * @throws java.lang.Exception
     */
    public StudentCourses findAllProjectedCourses(StudentCourses studentCourses) {
        //TODO: Make a new Rule class for Registered Courses
        List<StudentCourse> studentCourseList = new ArrayList<StudentCourse>();
        studentCourseList = studentCourses.getStudentCourseList();

        logger.debug("###################### Identifying PROJECTED COURSES ######################");

        for (StudentCourse studentCourse : studentCourseList) {
            String today = RuleEngineApiUtils.formatDate(new Date(), "yyyy-MM-dd");
            String sessionDate = studentCourse.getSessionDate() + "/01";
            Date temp = new Date();

            try {
                temp = RuleEngineApiUtils.parseDate(sessionDate, "yyyy/MM/dd");
                sessionDate = RuleEngineApiUtils.formatDate(temp, "yyyy-MM-dd");
            }catch (ParseException pe) {
                logger.error("ERROR: " + pe.getMessage());
            }

            int diff = RuleEngineApiUtils.getDifferenceInMonths(today, sessionDate);

            if ("".compareTo(studentCourse.getCompletedCourseLetterGrade().trim()) == 0
                    && diff < 1) {
                studentCourse.setProjected(true);
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

        //TODO: Make a new Rule class for Failed Courses
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

        //TODO: Make a new Rule class for Duplicate Courses
        // logger.debug("###################### Identifying Duplicates ######################");

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

    /**
     * Find all the Career Program courses
     *
     * @return StudentCourses
     * @throws java.lang.Exception
     */
    public StudentCourses findCareerProgramCourses(StudentCourses studentCourses) {

        //TODO: Make a new Rule class for Career Program Courses
        List<StudentCourse> studentCourseList = new ArrayList<StudentCourse>();
        studentCourseList = studentCourses.getStudentCourseList();

        logger.debug("###################### Identifying CAREER PROGRAM courses ######################");

        for (StudentCourse studentCourse : studentCourseList) {
            if (studentCourse.getCourseCode().startsWith("CP")) {
                studentCourse.setCareerPrep(true);
            }
        }

        studentCourses.setStudentCourseList(studentCourseList);

        return studentCourses;
    }

    /**
     * Find all the Locally Developed courses
     *
     * @return StudentCourses
     * @throws java.lang.Exception
     */
    public StudentCourses findAllLocallyDevelopedCourses(StudentCourses studentCourses) {

        //TODO: Make a new Rule class for Locally Developed Courses
        List<StudentCourse> studentCourseList = new ArrayList<StudentCourse>();
        studentCourseList = studentCourses.getStudentCourseList();

        logger.debug("###################### Identifying LOCALLY DEVELOPED courses ######################");

        for (StudentCourse studentCourse : studentCourseList) {
            if (studentCourse.getCourseCode().startsWith("X")) {
                studentCourse.setLocallyDeveloped(true);
            }
        }

        studentCourses.setStudentCourseList(studentCourseList);

        return studentCourses;
    }

    public RuleData runMinCreditsRule(MinCreditRuleData minCreditRuleInput) {

        String ruleType = minCreditRuleInput.getGradProgramRule().getRequirementType();
        logger.debug("Rule Type: " + ruleType);
        Rule rule = ruleFactory.createRule(RuleType.MIN_CREDITS, minCreditRuleInput);
        ((MinCreditsRule)rule).setInputData(minCreditRuleInput);
        MinCreditRuleData result = (MinCreditRuleData) ruleFactory.createRuleEngine(rule).fireRules();

        return result;
    }

    public RuleData runMatchRules(MatchRuleData matchRuleInput) {

        String ruleType = "M";
        Rule rule = ruleFactory.createRule(RuleType.MATCH, matchRuleInput);
        ((MatchRule)rule).setInputData(matchRuleInput);
        RuleData result = ruleFactory.createRuleEngine(rule).fireRules();

        /*for (ProgramRule programRule : matchRuleInput.getProgramRules().getProgramRuleList()){
            rule = ruleFactory.createRule(RuleType.MATCH, matchRuleInput);
            result = ruleFactory.createRuleEngine(rule).fireRules();
        }*/

        return result;
    }

    public RuleData runMinElectiveCreditsRule(MinElectiveCreditRuleData minElectiveCreditRuleInput) {

        String ruleType = minElectiveCreditRuleInput.getGradProgramRule().getRequirementType();
        Rule rule = ruleFactory.createRule(RuleType.MIN_CREDITS_ELECTIVE, minElectiveCreditRuleInput);
        ((MinElectiveCreditsRule)rule).setInputData(minElectiveCreditRuleInput);
        RuleData result = ruleFactory.createRuleEngine(rule).fireRules();

        return result;
    }
}
