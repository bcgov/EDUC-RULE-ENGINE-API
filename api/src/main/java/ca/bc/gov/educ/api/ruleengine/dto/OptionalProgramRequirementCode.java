package ca.bc.gov.educ.api.ruleengine.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Data
@Component
public class OptionalProgramRequirementCode implements Serializable {

	private String optProReqCode; 
	private String label; 
	private String description;
	private RequirementTypeCode requirementTypeCode;
	private String requiredCredits;
	private String notMetDesc;
	private String requiredLevel;
	private String languageOfInstruction;
	private String activeRequirement;
	private String requirementCategory;
	private String traxReqNumber;
	private boolean passed;
}
