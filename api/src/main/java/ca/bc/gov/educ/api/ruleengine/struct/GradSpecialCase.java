package ca.bc.gov.educ.api.ruleengine.struct;

import java.sql.Date;

import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@Component
public class GradSpecialCase {

	private String specialCase;
	private String passFlag;	
	private String description;	
	private String createdBy;	
	private Date createdTimestamp;	
	private String updatedBy;	
	private Date updatedTimestamp;
	
	@Override
	public String toString() {
		return "GradSpecialCase [specialCase=" + specialCase + ", passFlag=" + passFlag + ", description=" + description
				+ ", createdBy=" + createdBy + ", createdTimestamp=" + createdTimestamp + ", updatedBy=" + updatedBy
				+ ", updatedTimestamp=" + updatedTimestamp + "]";
	}			
}
