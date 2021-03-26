package ca.bc.gov.educ.api.ruleengine.struct;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class GradAlgorithmGraduationStatus {

	private String pen;
    private String program;
    private String programCompletionDate;
    private String gpa;
    private boolean honoursFlag;
    private String certificateType1;
    private String certificateType2;
    private String certificateType1Date; 
    private String certificateType2Date; 
    private boolean recalculateFlag;
    private String schoolOfRecord;
    private String studentGrade;
}
