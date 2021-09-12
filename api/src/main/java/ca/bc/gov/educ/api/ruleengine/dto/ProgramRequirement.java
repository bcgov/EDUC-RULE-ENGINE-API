package ca.bc.gov.educ.api.ruleengine.dto;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class ProgramRequirement {

	private UUID programRequirementID; 
	private String graduationProgramCode; 
	private ProgramRequirementCode programRequirementCode;
}
