package ca.bc.gov.educ.api.ruleengine.dto;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class LetterGrade {

	private String grade; 
	private String gpaMarkValue; 
	private String passFlag; 
	
	
	@Override
	public String toString() {
		return "LetterGrade [grade=" + grade + ", gpaMarkValue=" + gpaMarkValue + ", passFlag="
				+ passFlag + "]";
	}
	
				
}
