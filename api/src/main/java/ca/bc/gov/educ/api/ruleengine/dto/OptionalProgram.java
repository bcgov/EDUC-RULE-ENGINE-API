package ca.bc.gov.educ.api.ruleengine.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Data
@Component
public class OptionalProgram implements Serializable {

	private UUID optionalProgramID; 
	private String optProgramCode; 
	private String optionalProgramName;
	private String description; 
	private int displayOrder; 
	private Date effectiveDate;
	private Date expiryDate;
	private String graduationProgramCode;
	
	
			
}
