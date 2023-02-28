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
public class Remove2CreditCoursesRule implements Rule {

	private static Logger logger = LoggerFactory.getLogger(Remove2CreditCoursesRule.class);

	@Autowired
	private RuleProcessorData ruleProcessorData;

	@Override
	public RuleData fire() {

		logger.debug("###################### Finding 2 Credit Courses ######################");

		List<StudentCourse> studentCourseList = RuleProcessorRuleUtils.getUniqueStudentCourses(
				ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());

		for (StudentCourse studentCourse : studentCourseList) {
			Integer credits = studentCourse.getCredits();
			if(credits < 4) {
				studentCourse.setLessCreditCourse(true);
			}
		}

		ruleProcessorData.setExcludedCourses(RuleProcessorRuleUtils.maintainExcludedCourses(studentCourseList,ruleProcessorData.getExcludedCourses(),ruleProcessorData.isProjected()));
		ruleProcessorData.setStudentCourses(studentCourseList);

		logger.debug("Removed 2 Credit Courses: {0} ",
				(int) studentCourseList.stream().filter(StudentCourse::isLessCreditCourse).count());

		return ruleProcessorData;
	}

	@Override
	public void setInputData(RuleData inputData) {
		ruleProcessorData = (RuleProcessorData) inputData;
		logger.debug("DuplicateAssessmentsRule: Rule Processor Data set.");
	}
}
