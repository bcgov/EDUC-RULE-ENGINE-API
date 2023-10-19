package ca.bc.gov.educ.api.ruleengine.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Objects;

@Data
@Component
public class StudentAssessment implements Serializable {

    private String pen;
    private String assessmentCode;
    private String assessmentName;
    private String sessionDate;
    private String gradReqMet;
    private String gradReqMetDetail;
    private String specialCase;
    private String exceededWriteFlag;
    private Double proficiencyScore;
    private String wroteFlag;
    private Double rawScore;
    private Double percentComplete;
    private Double irtScore;
    private boolean isFailed;
    private boolean isDuplicate;
    private boolean isUsed;
    private boolean isProjected;
    private boolean isNotCompleted;
    private String equivalentCode;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentAssessment that = (StudentAssessment) o;
        return getPen().equals(that.getPen()) && getAssessmentCode().equals(that.getAssessmentCode()) && Objects.equals(getSessionDate(), that.getSessionDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPen(), getAssessmentCode(), getSessionDate());
    }
}