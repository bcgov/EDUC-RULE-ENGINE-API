package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class MinElectiveCredits1996Rule implements Rule {

	private static Logger logger = LoggerFactory.getLogger(MinElectiveCredits1996Rule.class);

	@Autowired
	private RuleProcessorData ruleProcessorData;

	public RuleData fire() {
		int totalCredits = 0;
		int requiredCredits;

		List<GradRequirement> requirementsNotMet = new ArrayList<>();

		int ldCourseCounter = 0;
		List<StudentCourse> tempStudentCourseList = RuleProcessorRuleUtils
				.getUniqueStudentCourses(ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());
		List<StudentCourse> minCreditGrade12Courses = tempStudentCourseList.stream().filter(StudentCourse::isUsedInMinCreditRule).collect(Collectors.toList());
		List<StudentCourse> minCreditGrade12CoursesWithLeftOverCredits = minCreditGrade12Courses.stream()
				.filter(sc -> sc.isUsedInMinCreditRule() && (sc.getLeftOverCredits() != null && sc.getLeftOverCredits() > 0)).toList();
		tempStudentCourseList.removeAll(minCreditGrade12Courses);
		tempStudentCourseList.addAll(minCreditGrade12CoursesWithLeftOverCredits);
		tempStudentCourseList.sort(Comparator.comparing(StudentCourse::getCompletedCoursePercentage, Comparator.nullsLast(Comparator.reverseOrder())));

		List<ProgramRequirement> gradProgramRules = ruleProcessorData
				.getGradProgramRules().stream().filter(gpr -> "MCE".compareTo(gpr.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
						&& "Y".compareTo(gpr.getProgramRequirementCode().getActiveRequirement()) == 0 && "C".compareTo(gpr.getProgramRequirementCode().getRequirementCategory()) == 0)
				.distinct().toList();

		if (minCreditGrade12Courses.isEmpty() && tempStudentCourseList.isEmpty()) {
			logger.warn("!!!Empty list sent to Min Elective Credits Rule for processing");
			AlgorithmSupportRule.processEmptyCourseCondition(ruleProcessorData,ruleProcessorData.getGradProgramRules(),requirementsNotMet);
			return ruleProcessorData;
		}

		for (ProgramRequirement gradProgramRule : gradProgramRules) {
			if(gradProgramRule.getProgramRequirementCode().getRequiredLevel() == null) {
				requiredCredits = Integer.parseInt(gradProgramRule.getProgramRequirementCode().getRequiredCredits().trim()); // list

				// 1st: minGrade12CreditCourses
				Pair<Integer, Integer> counts = processCourse(gradProgramRule, requiredCredits, minCreditGrade12Courses, totalCredits, ldCourseCounter, true);
				totalCredits = counts.getLeft();
				ldCourseCounter = counts.getRight();

				// 2nd: the rest of courses + (minGrade12CreditCourses with leftOverCredit > 0)
				counts = processCourse(gradProgramRule, requiredCredits, tempStudentCourseList, totalCredits, ldCourseCounter, false);
				totalCredits = counts.getLeft();

				AlgorithmSupportRule.checkCredits1996(totalCredits,requiredCredits,gradProgramRule,ruleProcessorData);
				totalCredits = 0;
			}
		}

		if(ruleProcessorData.getGradProgram().getProgramCode().equalsIgnoreCase("1996-EN"))
			ruleProcessorData.getStudentCourses().addAll(ruleProcessorData.getExcludedCourses());
		return ruleProcessorData;
	}

	private Pair<Integer, Integer> processCourse(ProgramRequirement gradProgramRule, Integer requiredCredits, List<StudentCourse> studentCourses,
												 Integer totalCredits, Integer ldCourseCounter, boolean onlyMinGrade12CreditCourses) {
		for (StudentCourse sc : studentCourses) {
			if ((sc.isUsedInMatchRule() || sc.isUsedInMinCreditRule())
					&& (sc.getLeftOverCredits() != null && sc.getLeftOverCredits() > 0)) {
				if (sc.getCourseCode().startsWith("X")) {
					if (ldCourseCounter < 8) {
						ldCourseCounter += sc.getLeftOverCredits();
					} else {
						continue;
					}
				}
				if (totalCredits + sc.getLeftOverCredits() <= requiredCredits) {
					totalCredits += sc.getLeftOverCredits();
					int credits = onlyMinGrade12CreditCourses? sc.getLeftOverCredits() : sc.getCreditsUsedForGrad() + sc.getLeftOverCredits();
					sc.setCreditsUsedForGrad(credits > sc.getCredits() ? sc.getCredits() : credits);
				} else {
					int extraCredits = totalCredits + sc.getLeftOverCredits() - requiredCredits;
					totalCredits = requiredCredits;
					int credits = sc.getCreditsUsedForGrad() + sc.getLeftOverCredits() - extraCredits;
					sc.setCreditsUsedForGrad(credits > sc.getCredits() ? sc.getCredits() : credits);
				}
				AlgorithmSupportRule.setGradReqMet(sc, gradProgramRule);
				sc.setUsed(true);
			} else {
				if (!sc.isUsedInMatchRule()) {
					boolean extraCreditsUsed = false;
					int extraCreditsLDcrses = 0;
					if (sc.getCourseCode().startsWith("X")) {
						if (ldCourseCounter < 8) {
							if (ldCourseCounter + sc.getCredits() <= 8) {
								ldCourseCounter += sc.getCredits();
							} else {
								int extraCredits = ldCourseCounter + sc.getCredits() - 8;
								ldCourseCounter += extraCredits;
								extraCreditsLDcrses = extraCredits;
								extraCreditsUsed = true;
							}
						} else {
							continue;
						}
					}
					totalCredits = AlgorithmSupportRule.processExtraCredits(extraCreditsUsed, extraCreditsLDcrses, sc, totalCredits, requiredCredits);
					AlgorithmSupportRule.setGradReqMet(sc, gradProgramRule);
					sc.setUsed(true);
				}
			}
			if (Objects.equals(totalCredits, requiredCredits)) {
				break;
			}
		}
		return Pair.of(totalCredits, ldCourseCounter);
	}

	@Override
	public void setInputData(RuleData inputData) {
		ruleProcessorData = (RuleProcessorData) inputData;
	}

}
