package ca.bc.gov.educ.api.ruleengine.dto;

import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

import static ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiConstants.DEFAULT_DATE_FORMAT;

@Data
@Component
public class GradAlgorithmGraduationStatus implements Serializable {

	private String pen;
    private String program;
    private String programCompletionDate;
    private String gpa;
    private String honoursStanding;
    private String recalculateGradStatus; 
    private String schoolOfRecord;
    private UUID schoolOfRecordId;
    private String studentGrade;
    private String studentStatus;
    private UUID studentID;
    private String consumerEducationRequirementMet;
    @JsonFormat(pattern=DEFAULT_DATE_FORMAT)
    private LocalDate adultStartDate;

    public Date getAdultStartDate() {
        return RuleEngineApiUtils.toDate(adultStartDate);
    }
}
