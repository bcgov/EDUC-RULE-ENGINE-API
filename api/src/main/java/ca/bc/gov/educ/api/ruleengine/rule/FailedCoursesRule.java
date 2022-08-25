package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Data
@AllArgsConstructor
public class FailedCoursesRule implements Rule {

	private static Logger logger = Logger.getLogger(FailedCoursesRule.class.getName());

	private RuleProcessorData ruleProcessorData;

	@Override
	public RuleData fire() {

		List<StudentCourse> studentCourseList = RuleProcessorRuleUtils.getUniqueStudentCourses(ruleProcessorData.getStudentCourses(),ruleProcessorData.isProjected());

		logger.log(Level.INFO, "###################### Finding FAILED courses ######################");

		for (StudentCourse studentCourse : studentCourseList) {
			String finalLetterGrade = studentCourse.getCompletedCourseLetterGrade();
			if(finalLetterGrade != null) {
			boolean failed = ruleProcessorData.getLetterGradeList().stream()
					.anyMatch(lg -> lg.getGrade().compareTo(finalLetterGrade) == 0
							&& lg.getPassFlag().compareTo("N") == 0);

			if (failed)
				studentCourse.setFailed(true);
			}
		}
		ruleProcessorData.setExcludedCourses(RuleProcessorRuleUtils.maintainExcludedCourses(studentCourseList,ruleProcessorData.getExcludedCourses(),ruleProcessorData.isProjected()));
		ruleProcessorData.setStudentCourses(studentCourseList);

		logger.log(Level.INFO, "Failed Courses: {0} ",
				(int) studentCourseList.stream().filter(StudentCourse::isFailed).count());

		return ruleProcessorData;
	}

}
