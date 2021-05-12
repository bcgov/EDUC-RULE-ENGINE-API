package ca.bc.gov.educ.api.ruleengine.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.educ.api.ruleengine.struct.StudentAssessment;
import ca.bc.gov.educ.api.ruleengine.struct.StudentCourse;

public class RuleProcessorRuleUtils {

    private static final Logger logger = LoggerFactory.getLogger(RuleProcessorRuleUtils.class);

    public static List<StudentCourse> getUniqueStudentCourses(List<StudentCourse> studentCourses, boolean projected) {
        List<StudentCourse> uniqueStudentCourseList = new ArrayList<StudentCourse>();

        uniqueStudentCourseList = studentCourses
                .stream()
                .filter(sc -> !sc.isNotCompleted()
                        && !sc.isDuplicate()
                        && !sc.isFailed()
                        && !sc.isCareerPrep()
                        && !sc.isLocallyDeveloped()
                        && !sc.isRestricted())
                .collect(Collectors.toList());

        if (!projected) {
            logger.info("Excluding Registrations!");
            uniqueStudentCourseList = uniqueStudentCourseList
                    .stream()
                    .filter(sc -> !sc.isProjected())
                    .collect(Collectors.toList());
        } else
            logger.info("Including Registrations!");

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
                        || (!projected && sc.isProjected()))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
	public static <T> T cloneObject(T input) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();

        return (T) objectMapper
                .readValue(objectMapper.writeValueAsString(input), input.getClass());
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
            logger.info("Excluding Registrations!");
            uniqueStudentAssessmentList = uniqueStudentAssessmentList
                    .stream()
                    .filter(sc -> !sc.isProjected())
                    .collect(Collectors.toList());
        } else
            logger.info("Including Registrations!");

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

}
