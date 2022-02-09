package ca.bc.gov.educ.api.ruleengine.rule;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class RestrictedCoursesRule implements Rule {

    private static Logger logger = Logger.getLogger(RestrictedCoursesRule.class.getName());

    @Autowired
    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {
        logger.log(Level.INFO,"###################### Finding COURSE RESTRICTIONS ######################");
        
        
        List<StudentCourse> studentCourses = RuleProcessorRuleUtils.getUniqueStudentCourses(
                ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());
        List<CourseRestriction> restrictedCourses = ruleProcessorData.getCourseRestrictions();
        if(restrictedCourses != null && !restrictedCourses.isEmpty()) {
			for (int i = 0; i < studentCourses.size(); i++) {
				StudentCourse sCourse = studentCourses.get(i);
				String courseCode = studentCourses.get(i).getCourseCode();
				String courseLevel = studentCourses.get(i).getCourseLevel();
				List<CourseRestriction> shortenedList = getMinimizedRestrictedCourses(restrictedCourses, courseLevel, courseCode);

				if (!shortenedList.isEmpty()) {
					for (CourseRestriction courseRestriction : shortenedList) {
						String restrictedCourse = courseRestriction.getRestrictedCourse();
						StudentCourse tempCourseRestriction = studentCourses.stream()
								.filter(sc -> restrictedCourse.compareTo(sc.getCourseCode()) == 0)
								.findAny()
								.orElse(null);
						if (tempCourseRestriction != null
								&& !tempCourseRestriction.isRestricted()
								&& !sCourse.isRestricted()
								&& !courseCode.equalsIgnoreCase(restrictedCourse)) {

							compareCredits(sCourse, tempCourseRestriction, studentCourses, i);
						}
					}
				}

			}
		}
        ruleProcessorData.setStudentCourses(studentCourses);
		List<StudentCourse> excludedCourses = RuleProcessorRuleUtils.getExcludedStudentCourses(ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());
		ruleProcessorData.setExcludedCourses(excludedCourses);
        prepareCoursesForOptionalPrograms();
        logger.log(Level.INFO, "Restricted Courses: {0} ", (int) studentCourses.stream().filter(StudentCourse::isRestricted).count());
        return ruleProcessorData;
    }
    
    private List<CourseRestriction> getMinimizedRestrictedCourses(List<CourseRestriction> restrictedCourses, String courseLevel, String courseCode) {
    	List<CourseRestriction> shortenedList;
    	if(StringUtils.isNotBlank(courseLevel)) {
        	shortenedList = restrictedCourses.stream()
        			.filter(cR -> courseCode.compareTo(cR.getMainCourse()) == 0
        			&& courseLevel.compareTo(cR.getMainCourseLevel()) == 0)
        			.collect(Collectors.toList());
    	}else {
    		shortenedList = restrictedCourses.stream()
        			.filter(cR -> courseCode.compareTo(cR.getMainCourse()) == 0)
        			.collect(Collectors.toList());
    	}
    	return shortenedList;
    }
    private void compareCredits(StudentCourse sCourse, StudentCourse tempCourseRestriction, List<StudentCourse> studentCourses, int i) {
    	if(sCourse.getCredits().equals(tempCourseRestriction.getCredits())) {
			if(sCourse.getCompletedCoursePercentage().equals(tempCourseRestriction.getCompletedCoursePercentage())) {
				compareSessionDates(sCourse,tempCourseRestriction,studentCourses,i);
			}else studentCourses.get(i).setRestricted(sCourse.getCompletedCoursePercentage() <= tempCourseRestriction.getCompletedCoursePercentage());
		}else studentCourses.get(i).setRestricted(sCourse.getCredits() <= tempCourseRestriction.getCredits());
    }
    private void compareSessionDates(StudentCourse sCourse, StudentCourse tempCourseRestriction, List<StudentCourse> studentCourses, int i) {
    	if (RuleEngineApiUtils.parsingTraxDate(sCourse.getSessionDate())
                .compareTo(RuleEngineApiUtils.parsingTraxDate(tempCourseRestriction.getSessionDate())) < 0) {
			studentCourses.get(i).setRestricted(false);	                                
        } else if (RuleEngineApiUtils.parsingTraxDate(sCourse.getSessionDate())
                .compareTo(RuleEngineApiUtils.parsingTraxDate(tempCourseRestriction.getSessionDate())) >= 0) {
        	studentCourses.get(i).setRestricted(true);
        }
    }
    private void prepareCoursesForOptionalPrograms() {
    	List<StudentCourse> listCourses = ruleProcessorData.getStudentCourses();
        Map<String,OptionalProgramRuleProcessor> mapOptional = ruleProcessorData.getMapOptional();
		mapOptional.forEach((k,v)-> v.setStudentCoursesOptionalProgram(RuleEngineApiUtils.getClone(listCourses)));
    }
    
    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("RestrictedCoursesRule: Rule Processor Data set.");
    }    
}
