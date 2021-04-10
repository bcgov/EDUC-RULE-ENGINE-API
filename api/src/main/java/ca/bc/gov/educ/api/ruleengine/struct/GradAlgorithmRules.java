package ca.bc.gov.educ.api.ruleengine.struct;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class GradAlgorithmRules {

	private UUID id; 
	private String ruleName; 
	private String ruleImplementation;
	private String ruleDescription;
	private Integer sortOrder;
	private String programCode;
	private String isActive;	
	
}
