package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class FailedCoursesRule implements Rule {

	private static Logger logger = LoggerFactory.getLogger(FailedCoursesRule.class);

	@Autowired
	private RuleProcessorData ruleProcessorData;

	@Override
	public RuleData fire() {

		List<StudentCourse> studentCourseList = RuleProcessorRuleUtils.getUniqueStudentCourses(ruleProcessorData.getStudentCourses(),ruleProcessorData.isProjected());

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
		ruleProcessorData.setExcludedCourses(RuleProcessorRuleUtils.maintainExcludedCourses("FailedCoursesRule", studentCourseList,ruleProcessorData.getExcludedCourses(),ruleProcessorData.isProjected()));
		ruleProcessorData.setStudentCourses(studentCourseList);

		logger.debug("Failed Courses: {} ",
				(int) studentCourseList.stream().filter(StudentCourse::isFailed).count());

		return ruleProcessorData;
	}

	public void setInputData(RuleData inputData) {
		ruleProcessorData = (RuleProcessorData) inputData;
	}
}
