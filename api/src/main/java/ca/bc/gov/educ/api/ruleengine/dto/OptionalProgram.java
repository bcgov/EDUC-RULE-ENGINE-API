package ca.bc.gov.educ.api.ruleengine.dto;

import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class OptionalProgram {

	private UUID optionalProgramID; 
	private String optProgramCode; 
	private String optionalProgramName;
	private String description; 
	private int displayOrder; 
	private Date effectiveDate;
	private Date expiryDate;
	private String graduationProgramCode;
	
	
			
}
