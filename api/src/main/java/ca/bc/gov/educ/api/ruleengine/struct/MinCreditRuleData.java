package ca.bc.gov.educ.api.ruleengine.struct;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class MinCreditRuleData implements RuleData {
    private ProgramRule programRule;
    private StudentCourses studentCourses;
    private int acquiredCredits;
    private int requiredCredits;
    private boolean passed;
}
