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
    private List<GradAlgorithmRules> gradAlgorithmRules;
    private List<GradProgramRule> gradProgramRules;
    private List<GradSpecialProgramRule> gradSpecialProgramRulesFrenchImmersion;
    private List<GradSpecialProgramRule> gradSpecialProgramRulesAdvancedPlacement;
    private List<GradSpecialProgramRule> gradSpecialProgramRulesInternationalBaccalaureateBD;
    private List<GradSpecialProgramRule> gradSpecialProgramRulesInternationalBaccalaureateBC;
    private List<GradSpecialProgramRule> gradSpecialProgramRulesCareerProgram;
    private List<GradSpecialProgramRule> gradSpecialProgramRulesDualDogwood;
    private List<StudentCourse> studentCourses;
    private List<StudentCourse> excludedCourses;
    private List<StudentCourse> studentCoursesForFrenchImmersion;
    private List<StudentCourse> studentCoursesForCareerProgram;
    private List<StudentCourse> studentCoursesForDualDogwood;
    private List<StudentAssessment> excludedAssessments;
    private List<StudentAssessment> studentAssessments;
    private List<StudentAssessment> studentAssessmentsForDualDogwood;
    private List<CourseRequirement> courseRequirements;
    private List<CourseRestriction> courseRestrictions;
    private List<AssessmentRequirement> assessmentRequirements;
    private List<Assessment> assessmentList;
    private List<GradLetterGrade> gradLetterGradeList;
    private List<GradSpecialCase> gradSpecialCaseList;
    private List<GradRequirement> nonGradReasons;
    private List<GradRequirement> requirementsMet;
    private List<GradRequirement> nonGradReasonsSpecialProgramsFrenchImmersion;
    private List<GradRequirement> requirementsMetSpecialProgramsFrenchImmersion;
    private List<GradRequirement> nonGradReasonsSpecialProgramsCareerProgram;
    private List<GradRequirement> requirementsMetSpecialProgramsCareerProgram;
    private List<GradRequirement> nonGradReasonsSpecialProgramsDualDogwood;
    private List<GradRequirement> requirementsMetSpecialProgramsDualDogwood;
    private boolean isGraduated;
    private boolean isSpecialProgramFrenchImmersionGraduated;
    private boolean isSpecialProgramAdvancedPlacementGraduated;
    private boolean isSpecialProgramInternationalBaccalaureateGraduatedBD;
    private boolean isSpecialProgramInternationalBaccalaureateGraduatedBC;
    private boolean isSpecialProgramCareerProgramGraduated;
    private boolean isSpecialProgramDualDogwoodGraduated;
    private boolean hasSpecialProgramFrenchImmersion;
    private boolean hasSpecialProgramAdvancedPlacement;
    private boolean hasSpecialProgramInternationalBaccalaureateBD;
    private boolean hasSpecialProgramInternationalBaccalaureateBC;
    private boolean hasSpecialProgramCareerProgram;
    private boolean hasSpecialProgramDualDogwood;
    private GradAlgorithmGraduationStatus gradStatus;
    private SpecialGradAlgorithmGraduationStatus gradSpecialProgramStatus;
    private School school;
    private boolean isProjected;
}
