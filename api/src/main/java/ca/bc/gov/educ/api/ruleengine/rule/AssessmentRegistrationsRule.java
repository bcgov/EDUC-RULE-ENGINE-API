package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.OptionalProgramRuleProcessor;
import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentAssessment;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
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
import java.util.Map;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentRegistrationsRule implements Rule {

	private static Logger logger = LoggerFactory.getLogger(AssessmentRegistrationsRule.class);

	@Autowired
	private RuleProcessorData ruleProcessorData;

	@Override
	public RuleData fire() {

		List<StudentAssessment> studentAssessmentList =  RuleProcessorRuleUtils.getUniqueStudentAssessments(ruleProcessorData.getStudentAssessments(),ruleProcessorData.isProjected());

		for (StudentAssessment studentAssessment : studentAssessmentList) {
			String specialCase = StringUtils.isBlank(studentAssessment.getSpecialCase())? "" : studentAssessment.getSpecialCase();
			String wroteFlag = StringUtils.isBlank(studentAssessment.getWroteFlag())? "" : studentAssessment.getWroteFlag();
			if ("".compareTo(specialCase.trim()) == 0 && "".compareTo(wroteFlag.trim()) == 0) {
				studentAssessment.setProjected(true);
			}
		}

		ruleProcessorData.setExcludedAssessments(RuleProcessorRuleUtils.maintainExcludedAssessments(studentAssessmentList,ruleProcessorData.getExcludedAssessments(),ruleProcessorData.isProjected()));
		ruleProcessorData.setStudentAssessments(studentAssessmentList);

		logger.debug("Projected Assessments (Registrations): {} ",(int) studentAssessmentList.stream().filter(StudentAssessment::isProjected).count());
		prepareAssessmentForOptionalPrograms();
		return ruleProcessorData;
	}

	private void prepareAssessmentForOptionalPrograms() {
    	List<StudentAssessment> listAssessments = ruleProcessorData.getStudentAssessments();
		Map<String,OptionalProgramRuleProcessor> mapOptional = ruleProcessorData.getMapOptional();
		mapOptional.forEach((k,v)-> v.setStudentAssessmentsOptionalProgram(RuleEngineApiUtils.getAssessmentClone(listAssessments)));
	}

	@Override
	public void setInputData(RuleData inputData) {
		ruleProcessorData = (RuleProcessorData) inputData;
	}
}
