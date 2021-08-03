package ca.bc.gov.educ.api.ruleengine.dto;

import java.sql.Date;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class SpecialCase {

	private String spCase;	
	private String label;	
	private int displayOrder; 
	private String description;
	private String passFlag;
	private Date effectiveDate; 
	private Date expiryDate;
	
	@Override
	public String toString() {
		return "SpecialCase [spCase=" + spCase + ", label=" + label + ", displayOrder=" + displayOrder
				+ ", description=" + description + ", passFlag=" + passFlag + ", effectiveDate=" + effectiveDate
				+ ", expiryDate=" + expiryDate + "]";
	}
	
	
}
