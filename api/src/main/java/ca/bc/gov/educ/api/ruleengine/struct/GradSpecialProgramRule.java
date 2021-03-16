package ca.bc.gov.educ.api.ruleengine.struct;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class GradSpecialProgramRule {

	private UUID id;
    private String ruleCode; 
	private String requirementName;
	private String requirementType;
	private String requirementTypeDesc;
	private String requiredCredits;
	private String notMetDesc;
	private String requiredLevel;
	private String languageOfInstruction;
	private String requirementDesc;
	private String isActive;
	private UUID specialProgramID;
	private boolean passed;
	
	@Override
	public String toString() {
		return "GradSpecialProgramRule [id=" + id + ", ruleCode=" + ruleCode + ", requirementName=" + requirementName
				+ ", requirementType=" + requirementType + ", requirementTypeDesc=" + requirementTypeDesc
				+ ", requiredCredits=" + requiredCredits + ", notMetDesc=" + notMetDesc + ", requiredLevel="
				+ requiredLevel + ", languageOfInstruction=" + languageOfInstruction + ", requirementDesc="
				+ requirementDesc + ", isActive=" + isActive + ", specialProgramID=" + specialProgramID + "]";
	}
	
	
}
