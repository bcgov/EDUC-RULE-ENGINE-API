package ca.bc.gov.educ.api.ruleengine.struct;

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

    private GradStudent gradStudent;
    private List<GradAlgorithmRules> gradAlgorithmRules;
    private List<GradProgramRule> gradProgramRules;
    private List<GradSpecialProgramRule> gradSpecialProgramRulesFrenchImmersion;
    private List<GradSpecialProgramRule> gradSpecialProgramRulesAdvancedPlacement;
    private List<GradSpecialProgramRule> gradSpecialProgramRulesInternationalBaccalaureateBD;
    private List<GradSpecialProgramRule> gradSpecialProgramRulesInternationalBaccalaureateBC;
    private List<GradSpecialProgramRule> gradSpecialProgramRulesCareerProgram;
    private List<StudentCourse> studentCourses;
    private List<StudentCourse> studentCoursesForFrenchImmersion;
    private List<StudentCourse> studentCoursesForCareerProgram;
    private List<StudentAssessment> studentAssessments;
    private List<CourseRequirement> courseRequirements;
    private List<GradLetterGrade> gradLetterGradeList;
    private List<GradRequirement> nonGradReasons;
    private List<GradRequirement> requirementsMet;
    private List<GradRequirement> nonGradReasonsSpecialPrograms;
    private List<GradRequirement> requirementsMetSpecialPrograms;
    private boolean isGraduated;
    private boolean isSpecialProgramFrenchImmersionGraduated;
    private boolean isSpecialProgramAdvancedPlacementGraduated;
    private boolean isSpecialProgramInternationalBaccalaureateGraduated;
    private boolean isSpecialProgramCareerProgramGraduated;
    private boolean hasSpecialProgramFrenchImmersion;
    private boolean hasSpecialProgramAdvancedPlacement;
    private boolean hasSpecialProgramInternationalBaccalaureate;
    private boolean hasSpecialProgramCareerProgram;
    private GradAlgorithmGraduationStatus gradStatus;
    private SpecialGradAlgorithmGraduationStatus gradSpecialProgramStatus;
    private School school;
    private boolean isProjected;
}
