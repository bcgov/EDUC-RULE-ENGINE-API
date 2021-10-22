package ca.bc.gov.educ.api.ruleengine.dto;

import java.util.List;

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
    private List<OptionalProgramRequirement> gradOptionalProgramRulesFrenchImmersion;
    private List<OptionalProgramRequirement> gradOptionalProgramRulesAdvancedPlacement;
    private List<OptionalProgramRequirement> gradOptionalProgramRulesInternationalBaccalaureateBD;
    private List<OptionalProgramRequirement> gradOptionalProgramRulesInternationalBaccalaureateBC;
    private List<OptionalProgramRequirement> gradOptionalProgramRulesCareerProgram;
    private List<OptionalProgramRequirement> gradOptionalProgramRulesDualDogwood;
    private List<StudentCourse> studentCourses;
    private List<StudentCourse> excludedCourses;
    private List<StudentCourse> studentCoursesForFrenchImmersion;
    private List<StudentCourse> studentCoursesForCareerProgram;
    private List<StudentCourse> studentCoursesForDualDogwood;
    private List<StudentAssessment> excludedAssessments;
    private List<StudentAssessment> studentAssessments;
    private List<StudentAssessment> studentAssessmentsForDualDogwood;
    private List<StudentAssessment> studentAssessmentsForFrenchImmersion;
    private List<CourseRequirement> courseRequirements;
    private List<CourseRestriction> courseRestrictions;
    private List<AssessmentRequirement> assessmentRequirements;
    private List<Assessment> assessmentList;
    private List<GradRequirement> nonGradReasons;
    private List<GradRequirement> requirementsMet;
    private List<GradRequirement> nonGradReasonsOptionalProgramsFrenchImmersion;
    private List<GradRequirement> requirementsMetOptionalProgramsFrenchImmersion;
    private List<GradRequirement> nonGradReasonsOptionalProgramsCareerProgram;
    private List<GradRequirement> requirementsMetOptionalProgramsCareerProgram;
    private List<GradRequirement> nonGradReasonsOptionalProgramsDualDogwood;
    private List<GradRequirement> requirementsMetOptionalProgramsDualDogwood;
    private boolean isGraduated;
    private boolean isOptionalProgramFrenchImmersionGraduated;
    private boolean isOptionalProgramAdvancedPlacementGraduated;
    private boolean isOptionalProgramInternationalBaccalaureateGraduatedBD;
    private boolean isOptionalProgramInternationalBaccalaureateGraduatedBC;
    private boolean isOptionalProgramCareerProgramGraduated;
    private boolean isOptionalProgramDualDogwoodGraduated;
    private boolean hasOptionalProgramFrenchImmersion;
    private boolean hasOptionalProgramAdvancedPlacement;
    private boolean hasOptionalProgramInternationalBaccalaureateBD;
    private boolean hasOptionalProgramInternationalBaccalaureateBC;
    private boolean hasOptionalProgramCareerProgram;
    private boolean hasOptionalProgramDualDogwood;
    private GradAlgorithmGraduationStatus gradStatus;
    private OptionalGradAlgorithmGraduationStatus gradOptionalProgramStatus;
    private GraduationProgramCode gradProgram;
    private School school;
    private boolean isProjected;
}
