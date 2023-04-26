package ca.bc.gov.educ.api.ruleengine.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.UUID;

import static ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiConstants.DEFAULT_DATE_FORMAT;

@Data
@Component
public class GradAlgorithmGraduationStatus {

	private String pen;
    private String program;
    private String programCompletionDate;
    private String gpa;
    private String honoursStanding;
    private String recalculateGradStatus; 
    private String schoolOfRecord;
    private String studentGrade;
    private String studentStatus;
    private UUID studentID;
    private String consumerEducationRequirementMet;
    @JsonFormat(pattern=DEFAULT_DATE_FORMAT)
    private Date adultStartDate;
}
