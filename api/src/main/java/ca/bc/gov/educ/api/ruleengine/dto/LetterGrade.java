package ca.bc.gov.educ.api.ruleengine.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Data
@Component
public class LetterGrade implements Serializable {

	private String grade; 
	private String gpaMarkValue; 
	private String passFlag; 
	
	
	@Override
	public String toString() {
		return "LetterGrade [grade=" + grade + ", gpaMarkValue=" + gpaMarkValue + ", passFlag="
				+ passFlag + "]";
	}
	
				
}
