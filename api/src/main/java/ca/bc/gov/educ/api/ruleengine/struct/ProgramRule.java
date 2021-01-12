package ca.bc.gov.educ.api.ruleengine.struct;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Data
@Component
public class ProgramRule {

    private UUID id;
    private String code;
    private String requirementName;
    private String requiredCredits;
    private String notMetDescription;
    private String fkProgramSetId;
    private String programCode;
    private String requirementType;
    private String activeFlag;
    private String requiredLevel;
    private Date activeDate;
    private boolean passed;

    @Override
    public String toString() {
        return "\nProgramRule{" +
                "ID=" + id +
                ", requirementCode=" + code +
                ", requirementName='" + requirementName + '\'' +
                ", requiredCredits=" + requiredCredits +
                ", notMetDescription='" + notMetDescription + '\'' +
                ", fkProgramSetId='" + fkProgramSetId + '\'' +
                ", programCode='" + programCode + '\'' +
                ", requirementType='" + requirementType + '\'' +
                ", activeFlag='" + activeFlag + '\'' +
                ", requiredLevel=" + requiredLevel +
                ", activeDate=" + activeDate +
                ", Passed=" + passed +
                '}';
    }
}
