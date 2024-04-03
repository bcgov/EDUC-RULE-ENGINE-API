package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentAssessment;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class IncompleteAssessmentsRule implements Rule {
    private static Logger logger = LoggerFactory.getLogger(IncompleteAssessmentsRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    public RuleData fire() {

        List<StudentAssessment> studentAssessmentList =  RuleProcessorRuleUtils.getUniqueStudentAssessments(ruleProcessorData.getStudentAssessments(),ruleProcessorData.isProjected());

         for (StudentAssessment studentAssessment : studentAssessmentList) {
             String specialCase = StringUtils.isBlank(studentAssessment.getSpecialCase())? "" : studentAssessment.getSpecialCase();
             String wroteFlag = StringUtils.isBlank(studentAssessment.getWroteFlag())? "" : studentAssessment.getWroteFlag();
             if ("".compareTo(specialCase.trim()) == 0 && "N".compareTo(wroteFlag.trim()) == 0) {
                 studentAssessment.setNotCompleted(true);
             }
        }

        ruleProcessorData.setExcludedAssessments(RuleProcessorRuleUtils.maintainExcludedAssessments(studentAssessmentList,ruleProcessorData.getExcludedAssessments(),ruleProcessorData.isProjected()));
        ruleProcessorData.setStudentAssessments(studentAssessmentList);

        logger.debug("Not Completed Assessments: {}",(int) studentAssessmentList.stream().filter(StudentAssessment::isNotCompleted).count());

        return ruleProcessorData;
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
    }
}
