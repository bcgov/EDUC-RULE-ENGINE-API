package ca.bc.gov.educ.api.ruleengine.struct;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class MinElectiveCreditRuleData implements RuleData {
    private GradProgramRule programRule;
    private StudentCourses studentCourses;
    private int acquiredCredits;
    private int requiredCredits;
    private boolean passed;
}
