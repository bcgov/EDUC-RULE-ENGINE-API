package ca.bc.gov.educ.api.ruleengine.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
@Data
public class GradProgramRules  implements Serializable {
    List<GradProgramRule> gradProgramRuleList;
}
