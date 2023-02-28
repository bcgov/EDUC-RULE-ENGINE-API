package ca.bc.gov.educ.api.ruleengine.rule;

import java.util.List;

import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentAssessment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

		List<StudentAssessment> studentAssessmentList =  RuleProcessorRuleUtils.getUniqueStudentAssessments(ruleProcessorData.getStudentAssessments(),ruleProcessorData.isProjected());

		logger.debug("###################### Finding FAILED assessments ######################");

		for (StudentAssessment studentAssessment : studentAssessmentList) {

			boolean failed = ruleProcessorData.getSpecialCaseList().stream()
					.anyMatch(lg -> lg.getSpCase()
							.compareTo(studentAssessment.getSpecialCase() != null
									? studentAssessment.getSpecialCase().trim()
									: "") == 0
							&& lg.getPassFlag().compareTo("N") == 0);

			if (failed)
				studentAssessment.setFailed(true);
			String exceededWriteFlag;
            if(studentAssessment.getExceededWriteFlag() == null) {
            	exceededWriteFlag = "";
            }else {
            	exceededWriteFlag = studentAssessment.getExceededWriteFlag();
            }
			if ("Y".compareTo(exceededWriteFlag.trim()) == 0) {
				studentAssessment.setFailed(true);
			}
		}

		ruleProcessorData.setExcludedAssessments(RuleProcessorRuleUtils.maintainExcludedAssessments(studentAssessmentList,ruleProcessorData.getExcludedAssessments(),ruleProcessorData.isProjected()));
		ruleProcessorData.setStudentAssessments(studentAssessmentList);

		logger.debug("Failed Assessments: {0}",
				(int) studentAssessmentList.stream().filter(StudentAssessment::isFailed).count());

		return ruleProcessorData;
	}

	public void setInputData(RuleData inputData) {
		ruleProcessorData = (RuleProcessorData) inputData;
		logger.debug("FailedAssessmentsRule: Rule Processor Data set.");
	}
}
