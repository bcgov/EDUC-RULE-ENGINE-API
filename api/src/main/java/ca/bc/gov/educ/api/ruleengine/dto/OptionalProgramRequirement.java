package ca.bc.gov.educ.api.ruleengine.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.UUID;

@Data
@Component
public class OptionalProgramRequirement implements Serializable {

	private UUID optionalProgramRequirementID; 
	private OptionalProgram optionalProgramID; 
	private OptionalProgramRequirementCode optionalProgramRequirementCode;
}
