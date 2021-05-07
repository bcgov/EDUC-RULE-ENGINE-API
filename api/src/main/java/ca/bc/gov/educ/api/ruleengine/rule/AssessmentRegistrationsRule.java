package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.struct.RuleData;
import ca.bc.gov.educ.api.ruleengine.struct.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.struct.StudentAssessment;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentRegistrationsRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(LDCoursesRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {
        List<StudentAssessment> studentAssessmentList = new ArrayList<StudentAssessment>();
        studentAssessmentList = ruleProcessorData.getStudentAssessments();

        logger.debug("###################### Finding PROJECTED assessments (For Projected GRAD) ######################");

        for (StudentAssessment studentAssessment : studentAssessmentList) {
            String today = RuleEngineApiUtils.formatDate(new Date(), "yyyy-MM-dd");
            String sessionDate = studentAssessment.getSessionDate() + "/01";
            Date temp = new Date();

            try {
                temp = RuleEngineApiUtils.parseDate(sessionDate, "yyyy/MM/dd");
                sessionDate = RuleEngineApiUtils.formatDate(temp, "yyyy-MM-dd");
            } catch (ParseException pe) {
                logger.error("ERROR: " + pe.getMessage());
            }

            int diff = RuleEngineApiUtils.getDifferenceInMonths(sessionDate,today);
            String proficiencyScore = "0.0";
            if(studentAssessment.getProficiencyScore() == null) {
            	proficiencyScore = "0.0";
            }else {
            	proficiencyScore = studentAssessment.getProficiencyScore().toString();
            }
            String specialCase = "";
            if(studentAssessment.getSpecialCase() == null) {
            	specialCase = "";
            }else {
            	specialCase = studentAssessment.getSpecialCase();
            }
            if ("".compareTo(specialCase.trim()) == 0  
            		&& "".compareTo(studentAssessment.getExceededWriteFlag().trim()) == 0
            		&& "0.0".compareTo(proficiencyScore) == 0
                    && diff < 1) {
                studentAssessment.setProjected(true);
            }
        }

        ruleProcessorData.setStudentAssessments(studentAssessmentList);

        logger.info("Projected Assessments (Registrations): " +
                (int) studentAssessmentList
                        .stream()
                        .filter(StudentAssessment::isProjected)
                        .count());

        return ruleProcessorData;
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("AssessmentRegistrationsRule: Rule Processor Data set.");
    }
}
