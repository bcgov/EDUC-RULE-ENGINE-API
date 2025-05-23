package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiConstants;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.*;

import static ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiConstants.DATE_FORMAT;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationsDuplicateCrseRule extends BaseRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(RegistrationsDuplicateCrseRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {
        List<StudentCourse> studentCourseList = ruleProcessorData.getStudentCourses();

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("PST"), Locale.CANADA);
        String today = RuleEngineApiUtils.formatDate(cal.getTime(), RuleEngineApiConstants.DEFAULT_DATE_FORMAT);
        boolean inProgressCourse1 = false;
        boolean inProgressCourse2 = false;
        boolean isCompletedCourse1 = false;
        boolean isCompletedCourse2 = false;

        for (int i = 0; i < studentCourseList.size() - 1; i++) {
            for (int j = i + 1; j < studentCourseList.size(); j++) {
                    if (studentCourseList.get(i).getCourseCode().equals(studentCourseList.get(j).getCourseCode())
                            && !studentCourseList.get(i).isDuplicate()
                            && studentCourseList.get(i).getCourseLevel().equals(studentCourseList.get(j).getCourseLevel())
                            && !studentCourseList.get(j).isDuplicate()) {
                        // exclude completed courses (i.e. courses with both finalLG && finalPercentage populated)
                        isCompletedCourse1 = RuleEngineApiUtils.isCompletedCourse(studentCourseList.get(i).getCompletedCourseLetterGrade(), studentCourseList.get(i).getCompletedCoursePercentage());
                        isCompletedCourse2 = RuleEngineApiUtils.isCompletedCourse(studentCourseList.get(j).getCompletedCourseLetterGrade(), studentCourseList.get(j).getCompletedCoursePercentage());
                        if (isCompletedCourse1 || isCompletedCourse2) {
                            continue;
                        }
                        try {
                            Date sessionDate1 = toLastDayOfMonth(RuleEngineApiUtils.parseDate(studentCourseList.get(i).getSessionDate() + "/01", DATE_FORMAT));
                            Date sessionDate2 = toLastDayOfMonth(RuleEngineApiUtils.parseDate(studentCourseList.get(j).getSessionDate() + "/01", DATE_FORMAT));
                            String sDate1 = RuleEngineApiUtils.formatDate(sessionDate1, RuleEngineApiConstants.DEFAULT_DATE_FORMAT);
                            String sDate2 = RuleEngineApiUtils.formatDate(sessionDate2, RuleEngineApiConstants.DEFAULT_DATE_FORMAT);

                            int diff1 = RuleEngineApiUtils.getDifferenceInMonths(sDate1,today);
                            int diff2 = RuleEngineApiUtils.getDifferenceInMonths(sDate2,today);
                            inProgressCourse1 = diff1 <= 0;
                            inProgressCourse2 = diff2 <= 0;
                        } catch (ParseException e) {
                            logger.debug("Parse Error {}",e.getMessage());
                        }
                        
                        if(inProgressCourse1 && inProgressCourse2) {
                            logger.debug("comparing {} with {}  -> Duplicate FOUND - CourseID: {}-{}", studentCourseList.get(i).getCourseCode(), studentCourseList.get(j).getCourseCode(), studentCourseList.get(i).getCourseCode(), studentCourseList.get(i).getCourseLevel());
                            if (studentCourseList.get(i).getInterimPercent() > studentCourseList.get(j).getInterimPercent()) {
                                studentCourseList.get(i).setDuplicate(false);
                                studentCourseList.get(j).setDuplicate(true);
                            } else if (studentCourseList.get(i).getInterimPercent() < studentCourseList.get(j).getInterimPercent()) {
                                studentCourseList.get(i).setDuplicate(true);
                                studentCourseList.get(j).setDuplicate(false);
                            } else {
                                boolean decision = RuleEngineApiUtils.compareCourseSessionDates(studentCourseList.get(i).getSessionDate(), studentCourseList.get(j).getSessionDate());
                                if (decision) {
                                    studentCourseList.get(i).setDuplicate(false);
                                    studentCourseList.get(j).setDuplicate(true);
                                } else {
                                    studentCourseList.get(i).setDuplicate(true);
                                    studentCourseList.get(j).setDuplicate(false);
                                }
                            }
                        }
                    } //Do Nothing

            }
        }

        ruleProcessorData.setStudentCourses(studentCourseList);

        logger.debug("Registrations but Duplicates Courses: {}",(int) studentCourseList.stream().filter(sc-> sc.isDuplicate() && sc.isProjected()).count());

        return ruleProcessorData;
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
    }
}
