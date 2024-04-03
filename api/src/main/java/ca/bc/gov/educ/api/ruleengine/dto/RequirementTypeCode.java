package ca.bc.gov.educ.api.ruleengine.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;

@Data
@Component
public class RequirementTypeCode implements Serializable {

	private String reqTypeCode; 
	private String label; 
	private int displayOrder; 
	private String description;	
	private Date effectiveDate; 
	private Date expiryDate;
	
	
	@Override
	public String toString() {
		return "RequirementTypeCode [reqTypeCode=" + reqTypeCode + ", label=" + label + ", displayOrder=" + displayOrder
				+ ", description=" + description + ", effectiveDate=" + effectiveDate + ", expiryDate=" + expiryDate
				+ "]";
	}
	
	
	
}