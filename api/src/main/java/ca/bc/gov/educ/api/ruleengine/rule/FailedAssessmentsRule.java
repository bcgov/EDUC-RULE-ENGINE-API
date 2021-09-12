package ca.bc.gov.educ.api.ruleengine.rule;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

	private static Logger logger = Logger.getLogger(FailedAssessmentsRule.class.getName());

	@Autowired
	private RuleProcessorData ruleProcessorData;

	@Override
	public RuleData fire() {

		 List<StudentAssessment> studentAssessmentList = ruleProcessorData.getStudentAssessments();

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
			String exceededWriteFlag = "";
            if(studentAssessment.getExceededWriteFlag() == null) {
            	exceededWriteFlag = "";
            }else {
            	exceededWriteFlag = studentAssessment.getExceededWriteFlag();
            }
			if ("Y".compareTo(exceededWriteFlag.trim()) == 0) {
				studentAssessment.setFailed(true);
			}
		}

		ruleProcessorData.setStudentAssessments(studentAssessmentList);

		logger.log(Level.INFO, "Failed Assessments: {0}",
				(int) studentAssessmentList.stream().filter(StudentAssessment::isFailed).count());

		return ruleProcessorData;
	}

	public void setInputData(RuleData inputData) {
		ruleProcessorData = (RuleProcessorData) inputData;
		logger.info("FailedAssessmentsRule: Rule Processor Data set.");
	}
}
