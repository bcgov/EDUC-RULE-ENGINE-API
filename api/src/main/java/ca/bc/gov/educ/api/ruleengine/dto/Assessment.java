package ca.bc.gov.educ.api.ruleengine.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.sql.Date;

@Data
@Component
public class Assessment implements Serializable {

	private String assessmentCode;
    private String assessmentName;
    private String language;    
    private Date startDate;
    private Date endDate;
    
	@Override
	public String toString() {
		return "Assessment [assessmentCode=" + assessmentCode + ", assessmentName=" + assessmentName + ", language="
				+ language + ", startDate=" + startDate + ", endDate=" + endDate + "]";
	}
    
			
}
