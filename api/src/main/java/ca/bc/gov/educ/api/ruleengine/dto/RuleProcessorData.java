package ca.bc.gov.educ.api.ruleengine.dto;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleProcessorData implements RuleData {

	private GradSearchStudent gradStudent;
	private List<LetterGrade> letterGradeList;
    private List<SpecialCase> specialCaseList;
    private List<ProgramAlgorithmRule> algorithmRules;
    private List<ProgramRequirement> gradProgramRules;
    private Map<String,OptionalProgramRuleProcessor> mapOptional;
    private Map<String,Integer> map1996Crse;
    private List<StudentCourse> studentCourses;
    private List<StudentCourse> excludedCourses;
    private List<StudentAssessment> excludedAssessments;
    private List<StudentAssessment> studentAssessments;
    private List<CourseRequirement> courseRequirements;
    private List<CourseRestriction> courseRestrictions;
    private List<AssessmentRequirement> assessmentRequirements;
    private List<Assessment> assessmentList;
    private List<GradRequirement> nonGradReasons;
    private List<GradRequirement> requirementsMet;
    private boolean isGraduated;
    private GradAlgorithmGraduationStatus gradStatus;
    private OptionalGradAlgorithmGraduationStatus gradOptionalProgramStatus;
    private GraduationProgramCode gradProgram;
    private School school;
    private boolean isProjected;
    private List<StudentCareerProgram> cpList;
}
