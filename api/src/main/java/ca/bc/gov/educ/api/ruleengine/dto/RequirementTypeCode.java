package ca.bc.gov.educ.api.ruleengine.dto;

import java.util.Date;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class RequirementTypeCode {

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