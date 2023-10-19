package ca.bc.gov.educ.api.ruleengine.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper=false)
@Component
public class GraduationProgramCode implements Serializable {

	private String programCode; 
	private String programName; 
	private String description; 
	private int displayOrder; 
	private Date effectiveDate;
	private Date expiryDate;

}
