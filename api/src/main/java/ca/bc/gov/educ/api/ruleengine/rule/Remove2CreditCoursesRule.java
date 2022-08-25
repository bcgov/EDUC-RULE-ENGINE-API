package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Remove2CreditCoursesRule implements Rule {

	private static Logger logger = Logger.getLogger(Remove2CreditCoursesRule.class.getName());

	@Override
	public RuleData fire(RuleProcessorData ruleProcessorData) {

		logger.log(Level.INFO, "###################### Finding 2 Credit Courses ######################");

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

		logger.log(Level.INFO, "Removed 2 Credit Courses: {0} ",
				(int) studentCourseList.stream().filter(StudentCourse::isLessCreditCourse).count());

		return ruleProcessorData;
	}

}
