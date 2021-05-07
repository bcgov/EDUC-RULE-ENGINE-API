package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.struct.RuleData;
import ca.bc.gov.educ.api.ruleengine.struct.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.struct.StudentAssessment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class FailedAssessmentsRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(FailedAssessmentsRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {

        List<StudentAssessment> studentAssessmentList = new ArrayList<StudentAssessment>();
        studentAssessmentList = ruleProcessorData.getStudentAssessments();

        logger.debug("###################### Finding FAILED assessments ######################");

        for (StudentAssessment studentAssessment : studentAssessmentList) {

            boolean failed = ruleProcessorData.getGradSpecialCaseList()
                    .stream()
                    .anyMatch(lg -> lg.getSpecialCase().compareTo(studentAssessment.getSpecialCase() != null ? studentAssessment.getSpecialCase().trim() : "") == 0
                            && lg.getPassFlag().compareTo("N") == 0);

            if (failed)
                studentAssessment.setFailed(true);
            if ("Y".compareTo(studentAssessment.getExceededWriteFlag().trim()) == 0) {
            	studentAssessment.setFailed(true);
            }
        }

        ruleProcessorData.setStudentAssessments(studentAssessmentList);

        logger.info("Failed Assessments: " +
                (int) studentAssessmentList
                        .stream()
                        .filter(StudentAssessment::isFailed)
                        .count());

        return ruleProcessorData;
    }

    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("FailedAssessmentsRule: Rule Processor Data set.");
    }
}