package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiConstants;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.*;


public class RegistrationsFailedCrseRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(RegistrationsFailedCrseRule.class);

    @Override
    public RuleData fire(RuleProcessorData ruleProcessorData) {
        List<StudentCourse> studentCourseList = ruleProcessorData.getStudentCourses();

        logger.debug("###################### Finding Failed Registrations ######################");
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("PST"), Locale.CANADA);
        boolean inProgressCourse = false;
        for (StudentCourse studentCourse : studentCourseList) {
            try {
                Date sessionDate = RuleEngineApiUtils.parseDate(studentCourse.getSessionDate() + "/01", "yyyy/MM/dd");
                String sDate = RuleEngineApiUtils.formatDate(sessionDate, RuleEngineApiConstants.DEFAULT_DATE_FORMAT);
                String today = RuleEngineApiUtils.formatDate(cal.getTime(), RuleEngineApiConstants.DEFAULT_DATE_FORMAT);
                int diff = RuleEngineApiUtils.getDifferenceInMonths(sDate,today);

                inProgressCourse = diff <= 0;
            } catch (ParseException e) {
                logger.debug("Parse Error {}",e.getMessage());
            }
            if(inProgressCourse) {
                String finalLetterGrade = studentCourse.getInterimLetterGrade();
                if (finalLetterGrade != null) {
                    boolean failed = ruleProcessorData.getLetterGradeList().stream()
                            .anyMatch(lg -> lg.getGrade().compareTo(finalLetterGrade) == 0
                                    && lg.getPassFlag().compareTo("N") == 0);

                    if (failed)
                        studentCourse.setFailed(true);
                }
            }
        }

        ruleProcessorData.setExcludedCourses(RuleProcessorRuleUtils.maintainExcludedCourses(studentCourseList,ruleProcessorData.getExcludedCourses(),ruleProcessorData.isProjected()));
        ruleProcessorData.setStudentCourses(studentCourseList);

        logger.info("Registrations but Failed Courses: {}",(int) studentCourseList.stream().filter(sc-> sc.isDuplicate() && sc.isProjected()).count());

        return ruleProcessorData;
    }

}
