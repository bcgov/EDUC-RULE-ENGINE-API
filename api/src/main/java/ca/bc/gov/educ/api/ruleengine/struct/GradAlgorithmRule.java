package ca.bc.gov.educ.api.ruleengine.struct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradAlgorithmRule {
    private String id;
    private String ruleName;
    private String ruleImplementation;
    private String ruleDescription;
    private int sortOrder;

}
