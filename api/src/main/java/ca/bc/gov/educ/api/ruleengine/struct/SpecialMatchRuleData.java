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
public class SpecialMatchRuleData implements RuleData{
    private GradSpecialProgramRules gradSpecialProgramRules;
    private StudentCourses studentCourses;
    private CourseRequirements courseRequirements;
    private boolean passed = false;
    private List<GradRequirement> passMessages;
    private List<GradRequirement> failMessages;

    public SpecialMatchRuleData (GradSpecialProgramRules gradSpecialProgramRules, StudentCourses studentCourses, CourseRequirements courseRequirements) {
        this.gradSpecialProgramRules = gradSpecialProgramRules;
        this.studentCourses = studentCourses;
        this.courseRequirements = courseRequirements;
    }
}
