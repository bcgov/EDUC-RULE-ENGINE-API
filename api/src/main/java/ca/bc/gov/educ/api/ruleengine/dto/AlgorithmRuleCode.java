package ca.bc.gov.educ.api.ruleengine.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Data
@Component
public class AlgorithmRuleCode implements Serializable {

	private String algoRuleCode; 
	private String ruleImplementation; 
	private String label; 
	private String description;
	private Integer displayOrder;
	private String isActiveRule;
}
