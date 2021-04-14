package ca.bc.gov.educ.api.ruleengine.struct;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class SpecialGradAlgorithmGraduationStatus {

	private String pen;
    private UUID specialProgramID;
    private String studentSpecialProgramData;
    private String specialProgramCompletionDate;
    private StudentCourses specialStudentCourses;
    private boolean isSpecialGraduated;
    private List<GradRequirement> specialNonGradReasons;
    private List<GradRequirement> specialRequirementsMet;
}
