package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.struct.ProgramRule;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

@Data
public class MatchRule implements Rule {

    @Autowired
    private ProgramRule programRule;

    @Override
    public <T> boolean fire(T parameters) {
        return false;
    }
}
