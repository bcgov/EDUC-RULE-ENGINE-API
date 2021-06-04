package ca.bc.gov.educ.api.ruleengine.struct;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class CourseRestriction {

	private UUID courseRestrictionId;
	private String mainCourse; 
	private String mainCourseLevel;
	private String restrictedCourse; 
	private String restrictedCourseLevel;   
	private String restrictionStartDate; 
	private String restrictionEndDate;	
}
