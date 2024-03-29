package ca.bc.gov.educ.api.ruleengine.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class StudentCareerProgram implements Serializable {

	private UUID id;	
	private String careerProgramCode;	
	private String careerProgramName;
	private UUID studentID;
	
}
