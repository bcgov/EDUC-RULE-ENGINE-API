package ca.bc.gov.educ.api.ruleengine.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.UUID;

@Data
@Component
public class AssessmentRequirement implements Serializable {

	private UUID assessmentRequirementId;
	private String assessmentCode;   
	private AssessmentRequirementCode ruleCode;
}
