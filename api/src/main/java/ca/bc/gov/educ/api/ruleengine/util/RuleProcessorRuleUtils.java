package ca.bc.gov.educ.api.ruleengine.util;

import ca.bc.gov.educ.api.ruleengine.dto.StudentAssessment;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
	public static <T> T cloneObject(T input) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();

        return (T) objectMapper
                .readValue(objectMapper.writeValueAsString(input), input.getClass());
    }

    public static List<StudentCourse> maintainExcludedCourses(List<StudentCourse> currentList,List<StudentCourse> existingExcludedList, boolean projected) {
        List<StudentCourse> exclList = getExcludedStudentCourses(currentList, projected);
        if(existingExcludedList == null)
            existingExcludedList = new ArrayList<>();

        if(!exclList.isEmpty()) {
            for(StudentCourse sc:exclList) {
                StudentCourse tempCourse = existingExcludedList.stream()
                        .filter(sp -> sp.getCourseCode().compareTo(sc.getCourseCode()) == 0 && sp.getCourseLevel().compareTo(sc.getCourseLevel())==0 && sp.getSessionDate().compareTo(sc.getSessionDate())==0 )
                        .findAny()
                        .orElse(null);
                if(tempCourse == null) {
                    existingExcludedList.add(sc);
                }
            }
        }
        return existingExcludedList;

    }

    public static List<StudentAssessment> maintainExcludedAssessments(List<StudentAssessment> currentList,List<StudentAssessment> existingExcludedList, boolean projected) {
        List<StudentAssessment> exclList = getExcludedStudentAssessments(currentList, projected);
        if(existingExcludedList == null)
            existingExcludedList = new ArrayList<>();

        if(!exclList.isEmpty()) {
            for(StudentAssessment sc:exclList) {
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
                if (dateFormat.parse(studentCourse.getSessionDate() + "/01").compareTo(gradDate) > 0) {
                    gradDate = dateFormat.parse(studentCourse.getSessionDate() + "/01");
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
}
