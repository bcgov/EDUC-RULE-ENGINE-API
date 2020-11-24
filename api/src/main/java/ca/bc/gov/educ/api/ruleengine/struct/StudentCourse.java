package ca.bc.gov.educ.api.ruleengine.struct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentCourse {

	private String pen;
    private String courseCode;
    private String courseName;
    private String courseLevel;
    private String sessionDate;
	private String courseDescription;
	private String gradReqMet;
	private Double completedCoursePercentage;
	private String completedCourseLetterGrade;
	private Double interimPercent;
	private String interimLetterGrade;
	private Integer credits;
	private Integer creditsUsedForGrad;	
	private String relatedCourse;
	private String relatedLevel;
	private String hasRelatedCourse;
	private boolean isNotCompleted;
	private boolean isFailed;
	private boolean isDuplicate;
	
	@Override
	public String toString() {
		return "StudentCourse [pen=" + pen + ", courseCode=" + courseCode + ", courseName=" + courseName
				+ ", courseLevel=" + courseLevel + ", sessionDate=" + sessionDate + ", courseDescription="
				+ courseDescription + ", gradReqMet=" + gradReqMet + ", completedCoursePercentage="
				+ completedCoursePercentage + ", completedCourseLetterGrade=" + completedCourseLetterGrade
				+ ", interimPercent=" + interimPercent + ", interimLetterGrade=" + interimLetterGrade + ", credits="
				+ credits + ", creditsUsedForGrad=" + creditsUsedForGrad + ", relatedCourse=" + relatedCourse
				+ ", relatedLevel=" + relatedLevel + ", hasRelatedCourse=" + hasRelatedCourse + "]";
	}
	
	
		
}
