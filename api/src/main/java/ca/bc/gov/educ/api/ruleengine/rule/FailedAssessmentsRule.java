package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentAssessment;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Data
@AllArgsConstructor
public class FailedAssessmentsRule implements Rule {

	private static Logger logger = Logger.getLogger(FailedAssessmentsRule.class.getName());

	private RuleProcessorData ruleProcessorData;

	@Override
	public RuleData fire() {

		List<StudentAssessment> studentAssessmentList =  RuleProcessorRuleUtils.getUniqueStudentAssessments(ruleProcessorData.getStudentAssessments(),ruleProcessorData.isProjected());

		logger.log(Level.INFO, "###################### Finding FAILED assessments ######################");

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

		logger.log(Level.INFO, "Failed Assessments: {0}",
				(int) studentAssessmentList.stream().filter(StudentAssessment::isFailed).count());

		return ruleProcessorData;
	}

}
