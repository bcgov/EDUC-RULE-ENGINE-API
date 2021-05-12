package ca.bc.gov.educ.api.ruleengine.rule;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.ruleengine.struct.CourseRestriction;
import ca.bc.gov.educ.api.ruleengine.struct.RuleData;
import ca.bc.gov.educ.api.ruleengine.struct.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.struct.StudentCourse;
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
        List<StudentCourse> excludedCourses = RuleProcessorRuleUtils.getExcludedStudentCourses(ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());
        ruleProcessorData.setExcludedCourses(excludedCourses);
        List<CourseRestriction> restrictedCourses = ruleProcessorData.getCourseRestrictions();
        for (int i = 0; i < studentCourses.size(); i++) {
        	StudentCourse sCourse =  studentCourses.get(i);
        	String courseCode = studentCourses.get(i).getCourseCode();
        	String courseLevel = studentCourses.get(i).getCourseLevel();
        	List<CourseRestriction> shortenedList = getMinimizedRestrictedCourses(restrictedCourses,courseLevel,courseCode);
        	
        	if(!shortenedList.isEmpty()) {
        		for (int j = 0; j < shortenedList.size(); j++) {
        			String restrictedCourse = shortenedList.get(j).getRestrictedCourse();
	        		StudentCourse tempCourseRestriction = studentCourses.stream()
	                        .filter(sc -> restrictedCourse.compareTo(sc.getCourseCode()) == 0)
	                        .findAny()
	                        .orElse(null);
	        		if(tempCourseRestriction != null 
	        				&& !tempCourseRestriction.isRestricted() 
	        				&& !sCourse.isRestricted() 
	        				&& !courseCode.equalsIgnoreCase(restrictedCourse)) {
        			
	        			compareCredits(sCourse,tempCourseRestriction,studentCourses,i);     			
	        		}
        		}
        	}
        	
        }
        ruleProcessorData.setStudentCourses(studentCourses);
        prepareCoursesForSpecialPrograms();
        logger.log(Level.INFO, "Restricted Courses: {0} ", (int) studentCourses.stream().filter(StudentCourse::isRestricted).count());
        return ruleProcessorData;
    }
    
    private List<CourseRestriction> getMinimizedRestrictedCourses(List<CourseRestriction> restrictedCourses, String courseLevel, String courseCode) {
    	List<CourseRestriction> shortenedList = null;
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
			}else if(sCourse.getCompletedCoursePercentage() > tempCourseRestriction.getCompletedCoursePercentage()) {
				studentCourses.get(i).setRestricted(false);
			}else {
				studentCourses.get(i).setRestricted(true);
			}
		}else if(sCourse.getCredits() > tempCourseRestriction.getCredits()) {
			studentCourses.get(i).setRestricted(false);	        				
		}else {
			studentCourses.get(i).setRestricted(true);	
		}
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
    private void prepareCoursesForSpecialPrograms() {
    	List<StudentCourse> listCourses = ruleProcessorData.getStudentCourses();
        if(ruleProcessorData.isHasSpecialProgramFrenchImmersion()) {
        	ruleProcessorData.setStudentCoursesForFrenchImmersion(RuleEngineApiUtils.getClone(listCourses));
        }
        if(ruleProcessorData.isHasSpecialProgramCareerProgram())
        	ruleProcessorData.setStudentCoursesForCareerProgram(RuleEngineApiUtils.getClone(listCourses));
    }
    
    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("RestrictedCoursesRule: Rule Processor Data set.");
    }    
}
