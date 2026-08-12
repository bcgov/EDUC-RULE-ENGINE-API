package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.OptionalProgramRuleProcessor;
import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentAssessment;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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

		List<StudentAssessment> studentAssessmentList = RuleProcessorRuleUtils.getUniqueStudentAssessments(ruleProcessorData.getStudentAssessments(), ruleProcessorData.isProjected());

		markFailedAssessments(studentAssessmentList);
		markOptionalProgramAssessmentsFailed();

		ruleProcessorData.setExcludedAssessments(RuleProcessorRuleUtils.maintainExcludedAssessments(studentAssessmentList,ruleProcessorData.getExcludedAssessments(),ruleProcessorData.isProjected()));
		ruleProcessorData.setStudentAssessments(studentAssessmentList);

		logger.debug("Failed Assessments: {}",
				(int) studentAssessmentList.stream().filter(StudentAssessment::isFailed).count());

		return ruleProcessorData;
	}

	private void markOptionalProgramAssessmentsFailed() {
		Map<String, OptionalProgramRuleProcessor> optionalPrograms = ruleProcessorData.getMapOptional();
		if (optionalPrograms == null) {
			return;
		}

		optionalPrograms.values().stream()
				.map(OptionalProgramRuleProcessor::getStudentAssessmentsOptionalProgram)
				.filter(Objects::nonNull)
				.forEach(this::markFailedAssessments);
	}

	private void markFailedAssessments(List<StudentAssessment> studentAssessments) {
		for (StudentAssessment studentAssessment : studentAssessments) {
			boolean failed = ruleProcessorData.getSpecialCaseList().stream()
					.anyMatch(specialCase -> specialCase.getSpCase()
							.compareTo(studentAssessment.getSpecialCase() != null
									? studentAssessment.getSpecialCase().trim()
									: "") == 0
							&& specialCase.getPassFlag().compareTo("N") == 0);

			String exceededWriteFlag = studentAssessment.getExceededWriteFlag() == null
					? ""
					: studentAssessment.getExceededWriteFlag();
			if (failed || "Y".equals(exceededWriteFlag.trim())) {
				studentAssessment.setFailed(true);
			}
		}
	}

	public void setInputData(RuleData inputData) {
		ruleProcessorData = (RuleProcessorData) inputData;
	}
}
