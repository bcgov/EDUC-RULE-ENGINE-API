package ca.bc.gov.educ.api.ruleengine.struct;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradRequirement {
    String rule;
    String description;
}
