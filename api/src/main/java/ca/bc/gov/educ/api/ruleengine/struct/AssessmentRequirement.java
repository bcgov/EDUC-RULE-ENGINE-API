package ca.bc.gov.educ.api.ruleengine.struct;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class AssessmentRequirement {

	private UUID assessmentRequirementId;
	private String assessmentCode;
    private String ruleCode;
    private String assessmentName;
}
