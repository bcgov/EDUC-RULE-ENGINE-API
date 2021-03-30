package ca.bc.gov.educ.api.ruleengine.util;

import ca.bc.gov.educ.api.ruleengine.struct.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.struct.StudentCourses;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                        && !sc.isLocallyDeveloped())
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
        List<StudentCourse> excludedStudentCourseList = new ArrayList<StudentCourse>();

        excludedStudentCourseList = studentCourses
                .stream()
                .filter(sc -> sc.isNotCompleted()
                        && sc.isDuplicate()
                        && sc.isFailed()
                        && sc.isCareerPrep()
                        && sc.isLocallyDeveloped())
                .collect(Collectors.toList());

        if (!projected) {
            logger.info("Excluding Registrations!");
            excludedStudentCourseList = excludedStudentCourseList
                    .stream()
                    .filter(StudentCourse::isProjected)
                    .collect(Collectors.toList());
        } else
            logger.info("Including Registrations!");

        return excludedStudentCourseList;
    }

    public static <T> T cloneObject(T input) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();

        return (T) objectMapper
                .readValue(objectMapper.writeValueAsString(input), input.getClass());
    }

}
