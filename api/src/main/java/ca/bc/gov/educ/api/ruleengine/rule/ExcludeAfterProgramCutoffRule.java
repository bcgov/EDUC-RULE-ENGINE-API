package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class ExcludeAfterProgramCutoffRule implements Rule {
    private static Logger logger = LoggerFactory.getLogger(ExcludeAfterProgramCutoffRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    public RuleData fire() {

         List<StudentCourse> studentCourseList = ruleProcessorData.getStudentCourses();

        logger.debug("###################### Finding Course Taken after program cutoff courses ######################");
        String cutoffDate = RuleEngineApiUtils.formatDate(ruleProcessorData.getGradProgram().getExpiryDate(), "yyyy-MM-dd");
        for (StudentCourse studentCourse : studentCourseList) {

            String sessionDate = studentCourse.getSessionDate() + "/01";
            try {
                Date temp = RuleEngineApiUtils.parseDate(sessionDate, "yyyy/MM/dd");
                sessionDate = RuleEngineApiUtils.formatDate(temp, "yyyy-MM-dd");
            } catch (ParseException pe) {
                logger.error("ERROR: {}",pe.getMessage());
            }

            int diff = RuleEngineApiUtils.getDifferenceInMonths(sessionDate,cutoffDate);

            if (diff < 0) {
                studentCourse.setCutOffCourse(true);
            }
        }

        ruleProcessorData.setStudentCourses(studentCourseList);

        logger.info("Cut-off Courses: {}",
                (int) studentCourseList
                        .stream()
                        .filter(StudentCourse::isCutOffCourse)
                        .count());

        return ruleProcessorData;
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("ExcludeAfterProgramCutoffRule: Rule Processor Data set.");
    }
}
