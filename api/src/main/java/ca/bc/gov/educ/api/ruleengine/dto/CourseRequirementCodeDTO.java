package ca.bc.gov.educ.api.ruleengine.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.sql.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class CourseRequirementCodeDTO implements Serializable {
    private String courseRequirementCode;
    private String label;
    private String description;
    private Date effectiveDate;
    private Date expiryDate;
}
