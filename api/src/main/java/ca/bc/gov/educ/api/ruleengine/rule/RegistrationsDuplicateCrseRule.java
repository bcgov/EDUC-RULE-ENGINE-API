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
import java.util.*;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationsDuplicateCrseRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(RegistrationsDuplicateCrseRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {
        List<StudentCourse> studentCourseList = ruleProcessorData.getStudentCourses();

        logger.debug("###################### Finding Duplicate Registrations ######################");
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("PST"), Locale.CANADA);
        boolean inProgressCourse1 = false;
        boolean inProgressCourse2 = false;
        for (int i = 0; i < studentCourseList.size() - 1; i++) {

            for (int j = i + 1; j < studentCourseList.size(); j++) {

                    if (studentCourseList.get(i).getCourseCode().equals(studentCourseList.get(j).getCourseCode())
                            && !studentCourseList.get(i).isDuplicate()
                            && studentCourseList.get(i).getCourseLevel().equals(studentCourseList.get(j).getCourseLevel())
                            && !studentCourseList.get(j).isDuplicate()) {
                        try {
                            Date sDate1 = RuleEngineApiUtils.parseDate(studentCourseList.get(i).getSessionDate() + "/01", "yyyy/MM/dd");
                            Date sDate2 = RuleEngineApiUtils.parseDate(studentCourseList.get(j).getSessionDate() + "/01", "yyyy/MM/dd");
                            Calendar calSDate1 = Calendar.getInstance();
                            calSDate1.setTime(sDate1);
                            Calendar calSDate2 = Calendar.getInstance();
                            calSDate2.setTime(sDate2);
                            inProgressCourse1 = cal.before(calSDate1);
                            inProgressCourse2 = cal.before(calSDate2);
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

        logger.info("Registrations but Duplicates Courses: {}",(int) studentCourseList.stream().filter(sc-> sc.isDuplicate() && sc.isProjected()).count());

        return ruleProcessorData;
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("RegistrationsDuplicateCrseRule: Rule Processor Data set.");
    }
}
