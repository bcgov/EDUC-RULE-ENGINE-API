package ca.bc.gov.educ.api.ruleengine.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
@Component
public class OptionalProgramRuleProcessor implements Serializable {

	private UUID optionalProgramID;

	private List<OptionalProgramRequirement> optionalProgramRules;
	private List<StudentCourse> studentCoursesOptionalProgram;
	private List<StudentAssessment> studentAssessmentsOptionalProgram;
	private List<GradRequirement> nonGradReasonsOptionalProgram;
	private List<GradRequirement> requirementsMetOptionalProgram;
	private String studentOptionalProgramData;
	private boolean isOptionalProgramGraduated;
	private String optionalProgramName;
	private boolean hasOptionalProgram;
}
