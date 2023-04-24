package ca.bc.gov.educ.api.ruleengine.rule;

import java.util.List;

import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class DuplicateCoursesRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(DuplicateCoursesRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {

        logger.debug("###################### Finding DUPLICATE courses ######################");
        List<StudentCourse> studentCourseList = RuleProcessorRuleUtils.getUniqueStudentCourses(ruleProcessorData.getStudentCourses(),ruleProcessorData.isProjected());

        for (int i = 0; i < studentCourseList.size() - 1; i++) {

            for (int j = i + 1; j < studentCourseList.size(); j++) {

                if (studentCourseList.get(i).getCourseCode().equals(studentCourseList.get(j).getCourseCode()) 
                		&& !studentCourseList.get(i).isDuplicate()
                        && studentCourseList.get(i).getCourseLevel().equals(studentCourseList.get(j).getCourseLevel())
                        && !studentCourseList.get(j).isDuplicate()) {

                	logger.debug("comparing {} with {}  -> Duplicate FOUND - CourseID: {}-{}",studentCourseList.get(i).getCourseCode(),studentCourseList.get(j).getCourseCode(),studentCourseList.get(i).getCourseCode(),studentCourseList.get(i).getCourseLevel());

                    if (studentCourseList.get(i).getCredits() > studentCourseList.get(j).getCredits()) {
                        studentCourseList.get(i).setDuplicate(false);
                        studentCourseList.get(j).setDuplicate(true);
                    } else if (studentCourseList.get(i).getCompletedCoursePercentage() > studentCourseList.get(j).getCompletedCoursePercentage()) {
                        studentCourseList.get(i).setDuplicate(false);
                        studentCourseList.get(j).setDuplicate(true);
                    } else if (studentCourseList.get(i).getCompletedCoursePercentage() < studentCourseList.get(j).getCompletedCoursePercentage()) {
                        studentCourseList.get(i).setDuplicate(true);
                        studentCourseList.get(j).setDuplicate(false);
                    } else if (studentCourseList.get(i).getCompletedCoursePercentage().equals(studentCourseList.get(j).getCompletedCoursePercentage())) {
                    	compareSessionDates(studentCourseList,i,j);                        
                    }
                }  //Do Nothing

            }
        }
        ruleProcessorData.setExcludedCourses(RuleProcessorRuleUtils.maintainExcludedCourses(studentCourseList,ruleProcessorData.getExcludedCourses(),ruleProcessorData.isProjected()));
        ruleProcessorData.setStudentCourses(studentCourseList);
        logger.debug("Duplicate Courses: {}",(int) studentCourseList.stream().filter(StudentCourse::isDuplicate).count());
        return ruleProcessorData;
    }
    
    private void compareSessionDates(List<StudentCourse> studentCourseList, int i, int j) {
    	if (RuleEngineApiUtils.parsingTraxDate(studentCourseList.get(i).getSessionDate())
                .before(RuleEngineApiUtils.parsingTraxDate(studentCourseList.get(j).getSessionDate()))) {
            studentCourseList.get(i).setDuplicate(false);
            studentCourseList.get(j).setDuplicate(true);
        }else {
            studentCourseList.get(i).setDuplicate(true);
            studentCourseList.get(j).setDuplicate(false);
        }
    }
    
    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.debug("DuplicateCoursesRule: Rule Processor Data set.");
    }
}
