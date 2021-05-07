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
    
}