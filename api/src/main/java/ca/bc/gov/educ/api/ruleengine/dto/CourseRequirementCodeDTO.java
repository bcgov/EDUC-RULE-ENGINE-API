package ca.bc.gov.educ.api.ruleengine.dto;

import java.sql.Date;

import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class CourseRequirementCodeDTO {
    private String courseRequirementCode;
    private String label;
    private String description;
    private Date effectiveDate;
    private Date expiryDate;
}
