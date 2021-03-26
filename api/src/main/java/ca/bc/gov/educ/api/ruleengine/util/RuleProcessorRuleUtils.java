package ca.bc.gov.educ.api.ruleengine.util;

import ca.bc.gov.educ.api.ruleengine.struct.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.struct.StudentCourses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

}
