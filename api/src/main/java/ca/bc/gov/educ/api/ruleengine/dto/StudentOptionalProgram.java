package ca.bc.gov.educ.api.ruleengine.dto;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class StudentOptionalProgram {

	private UUID id;
    private String pen;
    private UUID optionalProgramID;
    private String studentSpecialProgramData;
    private String specialProgramCompletionDate;
    private String specialProgramName;
    private String specialProgramCode;
    private String programCode;
    private UUID studentID;
				
}
