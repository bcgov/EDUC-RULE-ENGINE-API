package ca.bc.gov.educ.api.ruleengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * The type for Grad Student.
 */
@Component
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GradStudent implements Serializable {
    private String pen;
    private String archiveFlag;
    private String studSurname;
    private String studGiven;
    private String studMiddle;
    private String address1;
    private String address2;
    private String city;
    private String provinceCode;
    private String countryCode;
    private String postalCode;
    private String studBirth;
    private String studSex;
    private String mincode;

}
