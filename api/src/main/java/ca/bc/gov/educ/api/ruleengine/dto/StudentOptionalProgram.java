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
    private String studentOptionalProgramData;
    private String optionalProgramCompletionDate;
    private String optionalProgramName;
    private String optionalProgramCode;
    private String programCode;
    private UUID studentID;
				
}
