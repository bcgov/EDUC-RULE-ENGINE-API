package ca.bc.gov.educ.api.ruleengine.dto;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class AlgorithmRuleCode {

	private String algoRuleCode; 
	private String ruleImplementation; 
	private String label; 
	private String description;
	private Integer displayOrder;
	private String isActiveRule;
}
