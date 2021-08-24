package ca.bc.gov.educ.api.ruleengine.dto;

import java.util.Date;

import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@Component
public class GraduationProgramCode {

	private String programCode; 
	private String programName; 
	private String description; 
	private int displayOrder; 
	private Date effectiveDate;
	private Date expiryDate;
			
}
