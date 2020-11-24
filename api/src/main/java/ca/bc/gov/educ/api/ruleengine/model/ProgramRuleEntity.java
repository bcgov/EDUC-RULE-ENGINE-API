package ca.bc.gov.educ.api.ruleengine.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Entity
@Table(name = "program_rule")
public class ProgramRuleEntity {

    @Id
    @Column(name = "requirement_code", nullable = true)
    private Integer requirementCode;

    @Column(name = "requirement_name", nullable = true)
    private String requirementName;

    @Column(name = "required_credits", nullable = true)
    private Integer requiredCredits;

    @Column(name = "not_met_description", nullable = true)
    private String notMetDescription;

    @Column(name = "program_code", nullable = true)
    private String programCode;

    @Column(name = "requirement_type", nullable = true)
    private String requirementType;

    @Column(name = "active_flag", nullable = true)
    private String activeFlag;

    @Column(name = "required_level", nullable = true)
    private Integer requiredLevel;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_timestamp", nullable = false)
    private Date createdTimestamp;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    @Column(name = "updated_timestamp", nullable = false)
    private Date updatedTimestamp;
}
