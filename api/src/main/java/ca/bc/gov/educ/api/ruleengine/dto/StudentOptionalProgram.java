package ca.bc.gov.educ.api.ruleengine.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.UUID;

@Data
@Component
public class StudentOptionalProgram implements Serializable {

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
