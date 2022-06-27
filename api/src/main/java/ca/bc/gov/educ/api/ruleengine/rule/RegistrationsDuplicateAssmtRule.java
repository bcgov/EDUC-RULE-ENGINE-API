package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentAssessment;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiConstants;
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
public class RegistrationsDuplicateAssmtRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(RegistrationsDuplicateAssmtRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {
        List<StudentAssessment> studentAssessmentsList = ruleProcessorData.getStudentAssessments();

        logger.debug("###################### Finding Duplicate Registrations ######################");
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("PST"), Locale.CANADA);
        String today = RuleEngineApiUtils.formatDate(cal.getTime(), RuleEngineApiConstants.DEFAULT_DATE_FORMAT);
        boolean inProgressCourse1 = false;
        boolean inProgressCourse2 = false;
        for (int i = 0; i < studentAssessmentsList.size() - 1; i++) {

            for (int j = i + 1; j < studentAssessmentsList.size(); j++) {

                    if (studentAssessmentsList.get(i).getAssessmentCode().equals(studentAssessmentsList.get(j).getAssessmentCode())
                            && !studentAssessmentsList.get(i).isDuplicate()
                            && !studentAssessmentsList.get(j).isDuplicate()) {
                        try {
                            Date sessionDate1 = RuleEngineApiUtils.parseDate(studentAssessmentsList.get(i).getSessionDate() + "/01", "yyyy/MM/dd");
                            Date sessionDate2 = RuleEngineApiUtils.parseDate(studentAssessmentsList.get(j).getSessionDate() + "/01", "yyyy/MM/dd");
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
                            logger.debug("comparing {} with {}  -> Duplicate FOUND", studentAssessmentsList.get(i).getAssessmentCode(), studentAssessmentsList.get(j).getAssessmentCode());
                            boolean decision = RuleEngineApiUtils.compareCourseSessionDates(studentAssessmentsList.get(i).getSessionDate(), studentAssessmentsList.get(j).getSessionDate());
                            if (decision) {
                                studentAssessmentsList.get(i).setDuplicate(false);
                                studentAssessmentsList.get(j).setDuplicate(true);
                            } else {
                                studentAssessmentsList.get(i).setDuplicate(true);
                                studentAssessmentsList.get(j).setDuplicate(false);
                            }

                        }
                    } //Do Nothing

            }
        }

        ruleProcessorData.setExcludedAssessments(RuleProcessorRuleUtils.maintainExcludedAssessments(studentAssessmentsList,ruleProcessorData.getExcludedAssessments(),ruleProcessorData.isProjected()));
        ruleProcessorData.setStudentAssessments(studentAssessmentsList);

        logger.info("Registrations but Duplicates Asessments: {}",(int) studentAssessmentsList.stream().filter(sc-> sc.isDuplicate() && sc.isProjected()).count());

        return ruleProcessorData;
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("RegistrationsDuplicateAssmtRule: Rule Processor Data set.");
    }
}
