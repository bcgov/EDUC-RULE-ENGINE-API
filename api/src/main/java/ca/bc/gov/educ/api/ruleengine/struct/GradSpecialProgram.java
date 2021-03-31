package ca.bc.gov.educ.api.ruleengine.struct;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class GradSpecialProgram {

	private UUID id; 	
	private String specialProgramCode; 	
	private String specialProgramName; 	
	private String programCode;
	
	@Override
	public String toString() {
		return "GradSpecialProgram [id=" + id + ", specialProgramCode=" + specialProgramCode + ", specialProgramName="
				+ specialProgramName + ", programCode=" + programCode + "]";
	}
	
	
			
}
