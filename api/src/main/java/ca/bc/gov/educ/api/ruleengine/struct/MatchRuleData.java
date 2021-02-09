package ca.bc.gov.educ.api.ruleengine.struct;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class MatchRuleData implements RuleData {
    private GradProgramRules gradProgramRules;
    private StudentCourses studentCourses;
    private CourseRequirements courseRequirements;
    private boolean passed = false;
    private List<GradRequirement> passMessages;
    private List<GradRequirement> failMessages;

    public MatchRuleData (GradProgramRules gradProgramRules, StudentCourses studentCourses, CourseRequirements courseRequirements) {
        this.gradProgramRules = gradProgramRules;
        this.studentCourses = studentCourses;
        this.courseRequirements = courseRequirements;
    }
}
