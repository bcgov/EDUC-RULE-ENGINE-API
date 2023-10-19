package ca.bc.gov.educ.api.ruleengine.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.UUID;

@Data
@Component
public class CourseRestriction implements Serializable {

	private UUID courseRestrictionId;
	private String mainCourse; 
	private String mainCourseLevel;
	private String restrictedCourse; 
	private String restrictedCourseLevel;   
	private String restrictionStartDate; 
	private String restrictionEndDate;	
}
