package ca.bc.gov.educ.api.ruleengine.struct;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class StudentAssessment {

    private String pen;
    private String assessmentCode;
    private String assessmentName;
    private String sessionDate;
    private String gradReqMet;
    private String specialCase;
    private String exceededWriteFlag;
    private Double proficiencyScore;
    private String wroteFlag;
    private Double rawScore;
    private Double percentComplete;
    private Double irtScore;

    @Override
    public String toString() {
        return "StudentAssessment [pen=" + pen + ", assessmentCode=" + assessmentCode + ", sessionDate=" + sessionDate
                + ", gradReqMet=" + gradReqMet + ", specialCase=" + specialCase + ", exceededWriteFlag="
                + exceededWriteFlag + ", proficiencyScore=" + proficiencyScore + ", wroteFlag=" + wroteFlag
                + ", rawScore=" + rawScore + ", percentComplete=" + percentComplete + ", irtScore=" + irtScore + "]";
    }
}