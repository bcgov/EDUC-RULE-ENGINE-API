package ca.bc.gov.educ.api.ruleengine.rule;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.educ.api.ruleengine.struct.RuleData;
import ca.bc.gov.educ.api.ruleengine.struct.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.struct.StudentCourse;
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
        	ruleProcessorData.setStudentCoursesForFrenchImmersion(getClone(listCourses));
        }
        if(ruleProcessorData.isHasSpecialProgramCareerProgram())
        	ruleProcessorData.setStudentCoursesForCareerProgram(getClone(listCourses));
        return ruleProcessorData;
    }

    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.info("RestrictedCoursesRule: Rule Processor Data set.");
    }
    
    
    public List<StudentCourse> getClone(List<StudentCourse> listCourses) {
    	ObjectMapper mapper = new ObjectMapper();
		String json = "";

		try {
			json = mapper.writeValueAsString(listCourses);
			List<StudentCourse> cList = mapper.readValue(json, new TypeReference<List<StudentCourse>>(){});
			return cList;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
		
    }
}
