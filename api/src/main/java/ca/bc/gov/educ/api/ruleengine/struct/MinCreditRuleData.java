package ca.bc.gov.educ.api.ruleengine.struct;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class MinCreditRuleData {
    private ProgramRule programRule;
    private StudentCourses studentCourses;
}
