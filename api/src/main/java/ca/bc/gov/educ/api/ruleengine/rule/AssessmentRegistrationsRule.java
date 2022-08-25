package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.OptionalProgramRuleProcessor;
import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentAssessment;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Data
@AllArgsConstructor
public class AssessmentRegistrationsRule implements Rule {

	private static Logger logger = Logger.getLogger(AssessmentRegistrationsRule.class.getName());

	private RuleProcessorData ruleProcessorData;

	@Override
	public RuleData fire() {

		List<StudentAssessment> studentAssessmentList =  RuleProcessorRuleUtils.getUniqueStudentAssessments(ruleProcessorData.getStudentAssessments(),ruleProcessorData.isProjected());

		logger.log(Level.INFO,
				"###################### Finding PROJECTED assessments (For Projected GRAD) ######################");

		for (StudentAssessment studentAssessment : studentAssessmentList) {
			String today = RuleEngineApiUtils.formatDate(new Date(), "yyyy-MM-dd");
			String sessionDate = studentAssessment.getSessionDate() + "/01";
			try {
				Date temp = RuleEngineApiUtils.parseDate(sessionDate, "yyyy/MM/dd");
				sessionDate = RuleEngineApiUtils.formatDate(temp, "yyyy-MM-dd");
			} catch (ParseException pe) {
				logger.log(Level.SEVERE, "ERROR: {0}", pe.getMessage());
			}

			int diff = RuleEngineApiUtils.getDifferenceInMonths(sessionDate, today);
			String proficiencyScore;
			if (studentAssessment.getProficiencyScore() == null) {
				proficiencyScore = "0.0";
			} else {
				proficiencyScore = studentAssessment.getProficiencyScore().toString();
			}
			String specialCase;
			if (studentAssessment.getSpecialCase() == null) {
				specialCase = "";
			} else {
				specialCase = studentAssessment.getSpecialCase();
			}
			String exceededWriteFlag;
            if(studentAssessment.getExceededWriteFlag() == null) {
            	exceededWriteFlag = "";
            }else {
            	exceededWriteFlag = studentAssessment.getExceededWriteFlag();
            }
	            
			if ("".compareTo(specialCase.trim()) == 0
					&& "Y".compareTo(exceededWriteFlag.trim()) != 0
					&& "0.0".compareTo(proficiencyScore) == 0 && diff <= 0) {
				studentAssessment.setProjected(true);
			}
		}

		ruleProcessorData.setExcludedAssessments(RuleProcessorRuleUtils.maintainExcludedAssessments(studentAssessmentList,ruleProcessorData.getExcludedAssessments(),ruleProcessorData.isProjected()));
		ruleProcessorData.setStudentAssessments(studentAssessmentList);

		logger.log(Level.INFO, "Projected Assessments (Registrations): {0} ",(int) studentAssessmentList.stream().filter(StudentAssessment::isProjected).count());
		prepareAssessmentForOptionalPrograms();
		return ruleProcessorData;
	}

	private void prepareAssessmentForOptionalPrograms() {
    	List<StudentAssessment> listAssessments = ruleProcessorData.getStudentAssessments();
		Map<String,OptionalProgramRuleProcessor> mapOptional = ruleProcessorData.getMapOptional();
		mapOptional.forEach((k,v)-> v.setStudentAssessmentsOptionalProgram(RuleEngineApiUtils.getAssessmentClone(listAssessments)));
	}

}
