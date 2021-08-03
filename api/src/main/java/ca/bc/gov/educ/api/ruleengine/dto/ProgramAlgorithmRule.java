package ca.bc.gov.educ.api.ruleengine.dto;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class ProgramAlgorithmRule {

	private UUID programAlgoRuleID; 
	private String graduationProgramCode;
	private Integer sortOrder;	
	private AlgorithmRuleCode algorithmRuleCode;
}
