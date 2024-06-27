package ca.bc.gov.educ.api.ruleengine.util;

import ca.bc.gov.educ.api.ruleengine.dto.StudentAssessment;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class RuleProcessorRuleUtils {

    private RuleProcessorRuleUtils() {}

    private static final Logger logger = LoggerFactory.getLogger(RuleProcessorRuleUtils.class);

    public static List<StudentCourse> getUniqueStudentCourses(List<StudentCourse> studentCourses, boolean projected) {
         
         List<StudentCourse> uniqueStudentCourseList = studentCourses
                .stream()
                .filter(sc -> !sc.isNotCompleted()
                        && !sc.isDuplicate()
                        && !sc.isFailed()
                        && !sc.isCareerPrep()
                        && !sc.isLocallyDeveloped()
                        && !sc.isRestricted()
                        && !sc.isBoardAuthorityAuthorized()
                        && !sc.isIndependentDirectedStudies()
                        && !sc.isLessCreditCourse()
                        && !sc.isValidationCourse()
                        && !sc.isCutOffCourse()
                        && !sc.isGrade10Course())
                .collect(Collectors.toList());

        if (!projected) {
            logger.debug("Excluding Registrations!");
            uniqueStudentCourseList = uniqueStudentCourseList
                    .stream()
                    .filter(sc -> !sc.isProjected())
                    .collect(Collectors.toList());
        } else
            logger.debug("Including Registrations!");

        return uniqueStudentCourseList;
    }

    public static List<StudentCourse> getExcludedStudentCourses(List<StudentCourse> studentCourses, boolean projected) {
        return studentCourses
                .stream()
                .filter(sc -> sc.isNotCompleted()
                        || sc.isDuplicate()
                        || sc.isFailed()
                        || sc.isCareerPrep()
                        || sc.isLocallyDeveloped()
                        || sc.isRestricted()
                        || sc.isBoardAuthorityAuthorized()
                        || sc.isIndependentDirectedStudies()
                        || sc.isLessCreditCourse()
                        || sc.isValidationCourse()
                        || sc.isCutOffCourse()
                        || sc.isGrade10Course()
                        || (!projected && sc.isProjected()))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
	public static <T> T cloneObject(T input) {
        return (T) SerializationUtils.clone((Serializable) input);
    }

    public static List<StudentCourse> maintainExcludedCourses(String ruleName, List<StudentCourse> currentList, List<StudentCourse> existingExcludedList, boolean projected) {
        List<StudentCourse> excludedStudentCourses = getExcludedStudentCourses(currentList, projected);
        if(existingExcludedList == null) {
            existingExcludedList = new ArrayList<>();
        }

        if(!excludedStudentCourses.isEmpty()) {
            for(StudentCourse sc:excludedStudentCourses) {
                StudentCourse tempCourse = existingExcludedList.stream()
                        .filter(sp -> sp.getCourseCode().compareTo(sc.getCourseCode()) == 0 && sp.getCourseLevel().compareTo(sc.getCourseLevel())==0 && sp.getSessionDate().compareTo(sc.getSessionDate())==0 )
                        .findAny()
                        .orElse(null);
                if(tempCourse == null) {
                    logger.debug("{} added the course to the excluded list {}", ruleName, sc);
                    existingExcludedList.add(sc);
                }
            }
        }
        return existingExcludedList;

    }

    public static List<StudentAssessment> maintainExcludedAssessments(List<StudentAssessment> currentList,List<StudentAssessment> existingExcludedList, boolean projected) {
        List<StudentAssessment> excludedStudentAssessments = getExcludedStudentAssessments(currentList, projected);
        if(existingExcludedList == null)
            existingExcludedList = new ArrayList<>();

        if(!excludedStudentAssessments.isEmpty()) {
            for(StudentAssessment sc:excludedStudentAssessments) {
                StudentAssessment tempAssmt = existingExcludedList.stream()
                        .filter(sp -> sp.getAssessmentCode().compareTo(sc.getAssessmentCode()) == 0 && sp.getSessionDate().compareTo(sc.getSessionDate())==0)
                        .findAny()
                        .orElse(null);
                if(tempAssmt == null) {
                    existingExcludedList.add(sc);
                }
            }
        }
        return existingExcludedList;

    }

    public static List<StudentAssessment> getUniqueStudentAssessments(List<StudentAssessment> studentAssessments,
			boolean projected) {
        
         List<StudentAssessment> uniqueStudentAssessmentList = studentAssessments
                .stream()
                .filter(sc -> !sc.isNotCompleted()
                        && !sc.isDuplicate()
                        && !sc.isFailed())
                .collect(Collectors.toList());

        if (!projected) {
            logger.debug("Excluding Registrations!");
            uniqueStudentAssessmentList = uniqueStudentAssessmentList
                    .stream()
                    .filter(sc -> !sc.isProjected())
                    .collect(Collectors.toList());
        } else
            logger.debug("Including Registrations!");

        return uniqueStudentAssessmentList;
    }
    
    public static List<StudentAssessment> getExcludedStudentAssessments(List<StudentAssessment> studentAssessments, boolean projected) {
        return studentAssessments
                .stream()
                .filter(sc -> sc.isNotCompleted()
                        || sc.isDuplicate()
                        || sc.isFailed()
                        || (!projected && sc.isProjected()))
                .collect(Collectors.toList());
    }
    
    public static String getGradDate(List<StudentCourse> studentCourses) {

        Date gradDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

        try {
            gradDate = dateFormat.parse("1700/01/01");
        } catch (ParseException e) {
            logger.debug("Error {}",e.getMessage());
        }

        for (StudentCourse studentCourse : studentCourses) {
            try {
                Date dateTocompare = toLastDayOfMonth(dateFormat.parse(studentCourse.getSessionDate() + "/01"));
                if (dateTocompare.compareTo(gradDate) > 0) {
                    gradDate = dateTocompare;
                }
            } catch (ParseException e) {
                logger.debug("Error {}",e.getMessage());
            }
        }
        dateFormat = new SimpleDateFormat(RuleEngineApiConstants.DEFAULT_DATE_FORMAT);

        return dateFormat.format(gradDate);
    }
    
    public static String getCurrentDate() {

        Date gradDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat(RuleEngineApiConstants.DEFAULT_DATE_FORMAT);
        return dateFormat.format(gradDate);
    }
    
    public static String getProgramCompletionDate(Date pcd) {
    	DateFormat dateFormat = new SimpleDateFormat(RuleEngineApiConstants.DEFAULT_DATE_FORMAT);
        return dateFormat.format(pcd);
    }

    public static void updateCourseLevelForCLC(List<StudentCourse> studentCourses, String courseLevel) {
        studentCourses.forEach(sc -> {
            if (sc.getCourseCode().startsWith("CLC")) {
                sc.setCourseLevel(courseLevel);
            }
        });
    }

    static Date toLastDayOfMonth(Date date) {
        if(date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            return cal.getTime();
        }
        return null;
    }
}
