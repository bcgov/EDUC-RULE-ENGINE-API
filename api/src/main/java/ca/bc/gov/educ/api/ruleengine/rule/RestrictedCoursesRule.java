package ca.bc.gov.educ.api.ruleengine.rule;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class RestrictedCoursesRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(RestrictedCoursesRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {
        logger.debug("###################### Finding COURSE RESTRICTIONS ###################### - NOT YET IMPLEMENTED!!!");
        List<StudentCourse> listCourses = ruleProcessorData.getStudentCourses();
        if(ruleProcessorData.isHasSpecialProgramFrenchImmersion()) {
        	ruleProcessorData.setStudentCoursesForFrenchImmersion(RuleEngineApiUtils.getClone(listCourses));
        }
        if(ruleProcessorData.isHasSpecialProgramCareerProgram())
        	ruleProcessorData.setStudentCoursesForCareerProgram(RuleEngineApiUtils.getClone(listCourses));
        return ruleProcessorData;
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("RestrictedCoursesRule: Rule Processor Data set.");
    }    
}
