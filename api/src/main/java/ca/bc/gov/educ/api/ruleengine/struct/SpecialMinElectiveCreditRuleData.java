package ca.bc.gov.educ.api.ruleengine.struct;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class SpecialMinElectiveCreditRuleData implements RuleData {
    private GradSpecialProgramRules gradSpecialProgramRules;
    private StudentCourses studentCourses;
    private int acquiredCredits;
    private int requiredCredits;
    private boolean passed;
    private List<GradRequirement> passMessages;
    private List<GradRequirement> failMessages;
}
