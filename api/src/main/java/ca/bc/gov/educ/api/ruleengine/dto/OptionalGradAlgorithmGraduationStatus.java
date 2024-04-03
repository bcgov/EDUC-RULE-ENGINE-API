package ca.bc.gov.educ.api.ruleengine.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
@Component
public class OptionalGradAlgorithmGraduationStatus implements Serializable {

	private String pen;
    private UUID optionalProgramID;
    private String studentOptionalProgramData;
    private String optionalProgramCompletionDate;
    private StudentCourses optionalStudentCourses;
    private boolean isOptionalGraduated;
    private List<GradRequirement> optionalNonGradReasons;
    private List<GradRequirement> optionalRequirementsMet;
    private UUID studentID;
}
