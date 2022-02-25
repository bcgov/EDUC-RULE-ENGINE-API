package ca.bc.gov.educ.api.ruleengine.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
public class OptionalProgramRuleProcessor {

	private List<OptionalProgramRequirement> optionalProgramRules;
	private List<StudentCourse> studentCoursesOptionalProgram;
	private List<StudentAssessment> studentAssessmentsOptionalProgram;
	private List<GradRequirement> nonGradReasonsOptionalProgram;
	private List<GradRequirement> requirementsMetOptionalProgram;
	private boolean isOptionalProgramGraduated;
	private String optionalProgramName;
	private boolean hasOptionalProgram;
}
