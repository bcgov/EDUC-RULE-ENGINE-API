package ca.bc.gov.educ.api.ruleengine.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.UUID;

@Data
@Component
public class GradProgramRule implements Serializable {

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
    private String programCode;
    private String ruleCategory;
    private boolean passed;

    @Override
    public String toString() {
        return "GradProgramRule [id=" + id + ", ruleCode=" + ruleCode + ", requirementName=" + requirementName
                + ", requirementType=" + requirementType + ", requirementTypeDesc=" + requirementTypeDesc
                + ", requiredCredits=" + requiredCredits + ", notMetDesc=" + notMetDesc + ", requiredLevel="
                + requiredLevel + ", languageOfInstruction=" + languageOfInstruction + ", requirementDesc="
                + requirementDesc + ", isActive=" + isActive + ", programCode=" + programCode + ", ruleCategory="+ruleCategory+"]";
    }
}
