package ca.bc.gov.educ.api.ruleengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Objects;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradRequirement implements Serializable {
    String transcriptRule;
    String description;
    String rule;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GradRequirement that = (GradRequirement) o;
        return getRule().equals(that.getRule());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRule());
    }
}
