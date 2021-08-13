package ca.bc.gov.educ.api.ruleengine.rule;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.ruleengine.dto.ProgramRequirement;
import ca.bc.gov.educ.api.ruleengine.dto.GradRequirement;
import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class MinAdultCourses19Rule implements Rule {

	private static Logger logger = LoggerFactory.getLogger(MinAdultCourses19Rule.class);

	@Autowired
	private RuleProcessorData ruleProcessorData;

	public RuleData fire() {
		logger.debug("Min Adult Courses 19 Rule");

		int totalCredits = 0;
		int requiredCredits = 0;
		
		if (ruleProcessorData.getStudentCourses().isEmpty()) {
			logger.warn("!!!Empty list sent to Min Adult Courses Rule for processing");
			return ruleProcessorData;
		}

		List<StudentCourse> studentCourses = RuleProcessorRuleUtils
				.getUniqueStudentCourses(ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());
		String gradDate = RuleProcessorRuleUtils.getGradDate(studentCourses);
		int diff = RuleEngineApiUtils.getDifferenceInMonths(gradDate, "2012-07-01");
		if(diff < 0) {
			return ruleProcessorData;
		}

		logger.debug("Unique Courses: " + studentCourses.size());
		String dobOfStudent = ruleProcessorData.getGradStudent().getDob();
		List<ProgramRequirement> gradProgramRules = ruleProcessorData
				.getGradProgramRules().stream().filter(gpr -> "MAC19".compareTo(gpr.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
						&& "Y".compareTo(gpr.getProgramRequirementCode().getActiveRequirement()) == 0 && "C".compareTo(gpr.getProgramRequirementCode().getRequirementCategory()) == 0)
				.collect(Collectors.toList());

		logger.debug(gradProgramRules.toString());

		for (ProgramRequirement gradProgramRule : gradProgramRules) {
			requiredCredits = Integer.parseInt(gradProgramRule.getProgramRequirementCode().getRequiredCredits().trim()); // list

			List<StudentCourse> tempStudentCourseList = null;

			if (gradProgramRule.getProgramRequirementCode().getRequiredLevel() == null
					|| gradProgramRule.getProgramRequirementCode().getRequiredLevel().trim().compareTo("") == 0) {
				tempStudentCourseList = studentCourses.stream().filter(sc -> sc.isUsed()).collect(Collectors.toList());
			} else {
				tempStudentCourseList = studentCourses.stream()
						.filter(sc -> sc.isUsed()
								&& sc.getCourseLevel().compareTo(gradProgramRule.getProgramRequirementCode().getRequiredLevel().trim()) == 0)
						.collect(Collectors.toList());
			}

			for (StudentCourse sc : tempStudentCourseList) {
				String courseSessionDate = sc.getSessionDate() + "/01";
				Date temp = null;
				try {
					temp = RuleEngineApiUtils.parseDate(courseSessionDate, "yyyy/MM/dd");
				} catch (ParseException e) {
					e.getMessage();
				}
				int age = calculateAge(dobOfStudent,RuleEngineApiUtils.formatDate(temp, "yyyy-MM-dd"));
				if(age >= 19 && (totalCredits + sc.getCredits()) <= requiredCredits) {
						totalCredits += sc.getCredits();
						if (sc.getGradReqMet().length() > 0) {							
							sc.setGradReqMet(sc.getGradReqMet() + ", " + gradProgramRule.getProgramRequirementCode().getProReqCode());
							sc.setGradReqMetDetail(sc.getGradReqMetDetail() + ", " + gradProgramRule.getProgramRequirementCode().getProReqCode() + " - "
									+ gradProgramRule.getProgramRequirementCode().getLabel());
						} else {
							sc.setGradReqMet(gradProgramRule.getProgramRequirementCode().getProReqCode());
							sc.setGradReqMetDetail(
									gradProgramRule.getProgramRequirementCode().getProReqCode() + " - " + gradProgramRule.getProgramRequirementCode().getLabel());
						}
						sc.setUsed(true);
				}
				if (totalCredits == requiredCredits) {
					break;
				}
			}

			if (totalCredits >= requiredCredits) {
				logger.info(gradProgramRule.getProgramRequirementCode().getLabel() + " Passed");
				gradProgramRule.getProgramRequirementCode().setPassed(true);

				List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMet();

				if (reqsMet == null)
					reqsMet = new ArrayList<>();

				reqsMet.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getProReqCode(), gradProgramRule.getProgramRequirementCode().getLabel()));
				ruleProcessorData.setRequirementsMet(reqsMet);
				logger.debug("Min Adult Courses : Total-" + totalCredits + " Required-" + requiredCredits);

			} else {
				logger.info(gradProgramRule.getProgramRequirementCode().getDescription() + " Failed!");
				ruleProcessorData.setGraduated(false);

				List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasons();

				if (nonGradReasons == null)
					nonGradReasons = new ArrayList<>();

				nonGradReasons.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getProReqCode(), gradProgramRule.getProgramRequirementCode().getNotMetDesc()));
				ruleProcessorData.setNonGradReasons(nonGradReasons);
			}

			logger.info("Min Adult Courses -> Required:" + requiredCredits + " Has:" + totalCredits);
		}
		ruleProcessorData.getStudentCourses().addAll(ruleProcessorData.getExcludedCourses());
		return ruleProcessorData;
	}
	
	public int calculateAge(String dob,String sessionDate) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate birthDate = LocalDate.parse(dob, dateFormatter);
        LocalDate sDate = LocalDate.parse(sessionDate, dateFormatter);
        return Period.between(birthDate, sDate).getYears();
    }

	@Override
	public void setInputData(RuleData inputData) {
		ruleProcessorData = (RuleProcessorData) inputData;
		logger.info("MinAdultCourses19Rule: Rule Processor Data set.");
	}

}
