package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentAssessment;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DuplicateAssessmentsRule implements Rule {

	private static Logger logger = Logger.getLogger(DuplicateAssessmentsRule.class.getName());

	@Override
	public RuleData fire(RuleProcessorData ruleProcessorData) {

		logger.log(Level.INFO, "###################### Finding DUPLICATE Assessments ######################");

		List<StudentAssessment> studentAssessmentList =  RuleProcessorRuleUtils.getUniqueStudentAssessments(ruleProcessorData.getStudentAssessments(),ruleProcessorData.isProjected());
		getModifiedAndSortedData(studentAssessmentList);

		for (int i = 0; i < studentAssessmentList.size() - 1; i++) {
			for (int j = i + 1; j < studentAssessmentList.size(); j++) {
				if (studentAssessmentList.get(i).getEquivalentCode()
						.equals(studentAssessmentList.get(j).getEquivalentCode())
						&& !studentAssessmentList.get(i).isDuplicate() 
						&& !studentAssessmentList.get(j).isDuplicate()) {
					Double proficiencyScore1 = studentAssessmentList.get(i).getProficiencyScore() != null
							? studentAssessmentList.get(i).getProficiencyScore()
							: Double.valueOf("0.0");
					Double proficiencyScore2 = studentAssessmentList.get(j).getProficiencyScore() != null
							? studentAssessmentList.get(j).getProficiencyScore()
							: Double.valueOf("0.0");
					char specialCase1 = StringUtils.isNotBlank(studentAssessmentList.get(i).getSpecialCase())
							? studentAssessmentList.get(i).getSpecialCase().charAt(0)
							: Character.MIN_VALUE;
					char specialCase2 = StringUtils.isNotBlank(studentAssessmentList.get(j).getSpecialCase())
							? studentAssessmentList.get(j).getSpecialCase().charAt(0)
							: Character.MIN_VALUE;
					if (proficiencyScore1 > 0.0 && proficiencyScore2 > 0.0) {
						if (Double.compare(proficiencyScore1, proficiencyScore2) != 0) {
							compareDifferentProficiencyScore(proficiencyScore1, proficiencyScore2,
									studentAssessmentList, i, j);
						} else {
							compareSessionDates(studentAssessmentList.get(i).getSessionDate(),
									studentAssessmentList.get(j).getSessionDate(), studentAssessmentList, i, j);
						}
					} else if (proficiencyScore1 == 0.0 && proficiencyScore2 == 0.0) {
						if (specialCase1 == specialCase2) {
							compareSessionDates(studentAssessmentList.get(i).getSessionDate(),
									studentAssessmentList.get(j).getSessionDate(), studentAssessmentList, i, j);
						} else {
							compareSpecialCases(specialCase1, specialCase2, studentAssessmentList, i, j);
						}
					} else {
						compareDifferentProficiencyScore(proficiencyScore1, proficiencyScore2, studentAssessmentList, i,j);
					}
				}
			}
		}

		ruleProcessorData.setExcludedAssessments(RuleProcessorRuleUtils.maintainExcludedAssessments(studentAssessmentList,ruleProcessorData.getExcludedAssessments(),ruleProcessorData.isProjected()));
		ruleProcessorData.setStudentAssessments(studentAssessmentList);

		logger.log(Level.INFO, "Duplicate Assessments: {0}",
				(int) studentAssessmentList.stream().filter(StudentAssessment::isDuplicate).count());
		if(ruleProcessorData.getGradProgram().getProgramCode().equalsIgnoreCase("SCCP")
				|| ruleProcessorData.getGradProgram().getProgramCode().equalsIgnoreCase("1950")
				|| ruleProcessorData.getGradProgram().getProgramCode().equalsIgnoreCase("NOPROG")
				|| ruleProcessorData.getGradProgram().getProgramCode().contains("2004")) {
			RuleProcessorRuleUtils.getUniqueStudentAssessments(ruleProcessorData.getStudentAssessments(),ruleProcessorData.isProjected()).addAll(ruleProcessorData.getExcludedAssessments());
		}
		return ruleProcessorData;
	}

	private void getModifiedAndSortedData(List<StudentAssessment> studentAssessmentList) {
		studentAssessmentList.forEach(sA -> {
			if (sA.getAssessmentCode().equalsIgnoreCase("NME10") || sA.getAssessmentCode().equalsIgnoreCase("NME")) {
				sA.setEquivalentCode("NME");
			} else if (sA.getAssessmentCode().equalsIgnoreCase("NMF10")
					|| sA.getAssessmentCode().equalsIgnoreCase("NMF")) {
				sA.setEquivalentCode("NMF");
			} else {
				sA.setEquivalentCode(sA.getAssessmentCode());
			}
			if (sA.getProficiencyScore() == null) {
				sA.setProficiencyScore(0.0);
			}
			if (sA.getSpecialCase() == null) {
				sA.setSpecialCase("");
			}
		});
		studentAssessmentList.sort(Comparator.comparing(StudentAssessment::getPen).thenComparing(StudentAssessment::getEquivalentCode)
				.reversed().thenComparing(StudentAssessment::getProficiencyScore).reversed()
				.thenComparing(StudentAssessment::getSpecialCase)
				.thenComparing(StudentAssessment::getSessionDate));
	}

	public void compareSessionDates(String sessionDate1, String sessionDate2,
			List<StudentAssessment> studentAssessmentList, int i, int j) {
		if (RuleEngineApiUtils.parsingTraxDate(sessionDate1).before(RuleEngineApiUtils.parsingTraxDate(sessionDate2))) {
			studentAssessmentList.get(i).setDuplicate(false);
			studentAssessmentList.get(j).setDuplicate(true);
		} else {
			studentAssessmentList.get(i).setDuplicate(true);
			studentAssessmentList.get(j).setDuplicate(false);
		}
	}

	public void compareDifferentProficiencyScore(Double proficiencyScore1, Double proficiencyScore2,
			List<StudentAssessment> studentAssessmentList, int i, int j) {
		if (proficiencyScore1 > proficiencyScore2) {
			studentAssessmentList.get(i).setDuplicate(false);
			studentAssessmentList.get(j).setDuplicate(true);
		} else {
			studentAssessmentList.get(i).setDuplicate(true);
			studentAssessmentList.get(j).setDuplicate(false);
		}
	}

	public void compareSpecialCases(char specialCase1, char specialCase2, List<StudentAssessment> studentAssessmentList,
			int i, int j) {
		if (specialCase1 > specialCase2) {
			studentAssessmentList.get(i).setDuplicate(false);
			studentAssessmentList.get(j).setDuplicate(true);
		} else {
			studentAssessmentList.get(i).setDuplicate(true);
			studentAssessmentList.get(j).setDuplicate(false);
		}
	}

}
