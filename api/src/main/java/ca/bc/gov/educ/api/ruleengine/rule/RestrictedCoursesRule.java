package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class RestrictedCoursesRule implements Rule {

    private static Logger logger = Logger.getLogger(RestrictedCoursesRule.class.getName());

    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {
        logger.log(Level.INFO,"###################### Finding COURSE RESTRICTIONS ######################");
        
        
        List<StudentCourse> studentCourses = RuleProcessorRuleUtils.getUniqueStudentCourses(
                ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());
        List<CourseRestriction> restrictedCourses = ruleProcessorData.getCourseRestrictions();
		if(restrictedCourses == null) {
			restrictedCourses = new ArrayList<>();
		}
       	for (int i = 0; i < studentCourses.size(); i++) {
			StudentCourse sCourse = studentCourses.get(i);
			String courseCode = studentCourses.get(i).getCourseCode();
			String courseLevel = studentCourses.get(i).getCourseLevel();
			String sessionDate = studentCourses.get(i).getSessionDate();
			List<CourseRestriction> shortenedList = getMinimizedRestrictedCourses(restrictedCourses, courseLevel, courseCode,sessionDate);

			if (!shortenedList.isEmpty()) {
				for (CourseRestriction courseRestriction : shortenedList) {
					String restrictedCourse = courseRestriction.getRestrictedCourse().trim();
					String restrictedLevel = courseRestriction.getRestrictedCourseLevel().trim();
					StudentCourse tempCourseRestriction = studentCourses.stream()
								.filter(sc -> restrictedCourse.compareTo(sc.getCourseCode()) == 0 && restrictedLevel.compareTo(sc.getCourseLevel())==0 )
								.findAny()
								.orElse(null);
					if (tempCourseRestriction != null
							&& !tempCourseRestriction.isRestricted()
							&& !sCourse.isRestricted()
							&& (!courseCode.equalsIgnoreCase(restrictedCourse) || (courseCode.equalsIgnoreCase(restrictedCourse) && !courseLevel.equalsIgnoreCase(restrictedLevel)))) {
						compareCredits(sCourse, tempCourseRestriction, studentCourses, i);
					}
				}
			}

		}
		ruleProcessorData.setExcludedCourses(RuleProcessorRuleUtils.maintainExcludedCourses(studentCourses,ruleProcessorData.getExcludedCourses(),ruleProcessorData.isProjected()));
		ruleProcessorData.setStudentCourses(studentCourses);
        prepareCoursesForOptionalPrograms();
        logger.log(Level.INFO, "Restricted Courses: {0} ", (int) studentCourses.stream().filter(StudentCourse::isRestricted).count());
        return ruleProcessorData;
    }
    
    private List<CourseRestriction> getMinimizedRestrictedCourses(List<CourseRestriction> restrictedCourses, String courseLevel, String courseCode,String sessionDate) {
    	List<CourseRestriction> shortenedList;
    	if(StringUtils.isNotBlank(courseLevel)) {
        	shortenedList = restrictedCourses.stream()
        			.filter(cR -> courseCode.compareTo(cR.getMainCourse()) == 0
        			&& courseLevel.compareTo(cR.getMainCourseLevel()) == 0 && RuleEngineApiUtils.checkDateForRestrictedCourses(cR.getRestrictionStartDate(),cR.getRestrictionEndDate(),sessionDate))
        			.collect(Collectors.toList());
    	}else {
    		shortenedList = restrictedCourses.stream()
        			.filter(cR -> courseCode.compareTo(cR.getMainCourse()) == 0 && RuleEngineApiUtils.checkDateForRestrictedCourses(cR.getRestrictionStartDate(),cR.getRestrictionEndDate(),sessionDate))
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

}
