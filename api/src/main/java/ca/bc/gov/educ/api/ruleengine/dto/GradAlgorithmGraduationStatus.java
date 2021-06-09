package ca.bc.gov.educ.api.ruleengine.dto;

import lombok.Data;

import java.util.UUID;

import org.springframework.stereotype.Component;

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
}
