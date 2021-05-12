package ca.bc.gov.educ.api.ruleengine.rule;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.ruleengine.struct.RuleData;
import ca.bc.gov.educ.api.ruleengine.struct.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.struct.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class DuplicateCoursesRule implements Rule {

    private static Logger logger = Logger.getLogger(DuplicateCoursesRule.class.getName());

    @Autowired
    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {

        logger.log(Level.INFO,"###################### Finding DUPLICATE courses ######################");
        List<StudentCourse> studentCourseList = ruleProcessorData.getStudentCourses();

        for (int i = 0; i < studentCourseList.size() - 1; i++) {

            for (int j = i + 1; j < studentCourseList.size(); j++) {

                if (studentCourseList.get(i).getCourseCode().equals(studentCourseList.get(j).getCourseCode())
                        && studentCourseList.get(i).getCourseLevel().equals(studentCourseList.get(j).getCourseLevel())) {

                	logger.log(Level.SEVERE,"comparing " + studentCourseList.get(i).getCourseCode() + " with "
                            + studentCourseList.get(j).getCourseCode() + " -> Duplicate FOUND - CourseID:"
                            + studentCourseList.get(i).getCourseCode() + "-" + studentCourseList.get(i).getCourseLevel());

                    if (studentCourseList.get(i).getCompletedCoursePercentage() > studentCourseList.get(j).getCompletedCoursePercentage()) {
                        studentCourseList.get(i).setDuplicate(false);
                        studentCourseList.get(j).setDuplicate(true);
                    } else if (studentCourseList.get(i).getCompletedCoursePercentage() < studentCourseList.get(j).getCompletedCoursePercentage()) {
                        studentCourseList.get(i).setDuplicate(true);
                        studentCourseList.get(j).setDuplicate(false);
                    } else if (studentCourseList.get(i).getCompletedCoursePercentage().equals(studentCourseList.get(j).getCompletedCoursePercentage())) {
                    	compareSessionDates(studentCourseList,i,j);                        
                    }
                } else {
                    //Do Nothing
                }
            }
        }

        logger.log(Level.INFO,"Duplicate Courses: {0} ",(int) studentCourseList.stream().filter(StudentCourse::isDuplicate).count());
        return ruleProcessorData;
    }
    
    private void compareSessionDates(List<StudentCourse> studentCourseList, int i, int j) {
    	if (RuleEngineApiUtils.parsingTraxDate(studentCourseList.get(i).getSessionDate())
                .compareTo(RuleEngineApiUtils.parsingTraxDate(studentCourseList.get(j).getSessionDate())) < 0) {
            studentCourseList.get(i).setDuplicate(false);
            studentCourseList.get(j).setDuplicate(true);
        } else if (RuleEngineApiUtils.parsingTraxDate(studentCourseList.get(i).getSessionDate())
                .compareTo(RuleEngineApiUtils.parsingTraxDate(studentCourseList.get(j).getSessionDate())) >= 0) {
            studentCourseList.get(i).setDuplicate(true);
            studentCourseList.get(j).setDuplicate(false);
        }
    }
    
    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("DuplicateCoursesRule: Rule Processor Data set.");
    }
}
