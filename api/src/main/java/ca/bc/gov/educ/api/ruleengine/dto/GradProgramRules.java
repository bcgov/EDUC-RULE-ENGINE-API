package ca.bc.gov.educ.api.ruleengine.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
public class GradProgramRules {
    List<GradProgramRule> gradProgramRuleList;
}
