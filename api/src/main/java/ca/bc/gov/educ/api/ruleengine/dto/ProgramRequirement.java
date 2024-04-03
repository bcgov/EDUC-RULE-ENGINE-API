package ca.bc.gov.educ.api.ruleengine.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.UUID;

@Data
@Component
public class ProgramRequirement implements Serializable {

	private UUID programRequirementID; 
	private String graduationProgramCode; 
	private ProgramRequirementCode programRequirementCode;
}
