package ca.bc.gov.educ.api.ruleengine.dto;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class OptionalProgramRequirement {

	private UUID optionalProgramRequirementID; 
	private OptionalProgram optionalProgramID; 
	private OptionalProgramRequirementCode optionalProgramRequirementCode;
}
