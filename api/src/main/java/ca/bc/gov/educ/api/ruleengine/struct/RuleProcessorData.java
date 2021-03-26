package ca.bc.gov.educ.api.ruleengine.struct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleProcessorData implements RuleData {

    private GradStudent gradStudent;
    private List<GradAlgorithmRule> gradAlgorithmRules;
    private List<GradProgramRule> gradProgramRules;
    private List<StudentCourse> studentCourses;
    private List<StudentAssessment> studentAssessments;
    private List<CourseRequirement> courseRequirements;
    private List<GradLetterGrade> gradLetterGradeList;
    private List<GradRequirement> nonGradReasons;
    private List<GradRequirement> requirementsMet;
    private boolean isGraduated;
    private GradAlgorithmGraduationStatus gradStatus;
    private School school;
    private boolean isProjected;
}
