package ca.bc.gov.educ.api.ruleengine.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Component
public class GradAlgorithmGraduationStatus {

	private String pen;
    private String program;
    private String programCompletionDate;
    private String gpa;
    private String honoursStanding;
    private String recalculateGradStatus; 
    private String schoolOfRecord;
    private String studentGrade;
    private String studentStatus;
    private UUID studentID;
    private String consumerEducationRequirementMet;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate adultStartDate;
}
