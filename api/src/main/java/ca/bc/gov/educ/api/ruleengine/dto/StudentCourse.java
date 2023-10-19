package ca.bc.gov.educ.api.ruleengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentCourse implements Serializable {

    private String pen;
    private String courseCode;
    private String courseName;
    private Integer originalCredits;
    private String courseLevel;
    private String sessionDate;
    private String customizedCourseName;
    private String gradReqMet;
    private String gradReqMetDetail;
    private Double completedCoursePercentage;
    private String completedCourseLetterGrade;
    private Double interimPercent;
    private String interimLetterGrade;
    private Double bestSchoolPercent; 
    private Double bestExamPercent;
    private Double schoolPercent;
    private Double examPercent;
    private String equivOrChallenge;
    private String fineArtsAppliedSkills;    
    private String metLitNumRequirement; 
    private Integer credits;
    private Integer creditsUsedForGrad;
    private String relatedCourse;
    private String relatedCourseName;
    private String relatedLevel;
    private String hasRelatedCourse;
    private boolean isNotCompleted;
    private String genericCourseType;
    private String language;
    private String workExpFlag;
    private String specialCase;
    private String toWriteFlag;
    private String provExamCourse;
    private boolean isProjected;
    private boolean isFailed;
    private boolean isDuplicate;
    private boolean isCareerPrep;
    private boolean isLocallyDeveloped;
    private boolean isBoardAuthorityAuthorized;
    private boolean isIndependentDirectedStudies;
    private boolean isUsed;
    private boolean isRestricted;
    private boolean isNotEligibleForElective;
    private boolean isUsedInMatchRule;
    private boolean isLessCreditCourse;
    private boolean isValidationCourse;
    private boolean isCutOffCourse;
    private boolean isGrade10Course;
    private Integer leftOverCredits;

    public Integer getCreditsUsedForGrad() {
        if (creditsUsedForGrad == null)
            return 0;
        else
            return creditsUsedForGrad;
    }

    public String getCourseCode() {
        if (courseCode != null)
            courseCode = courseCode.trim();
        return courseCode;
    }

    public String getCourseLevel() {
        if (courseLevel != null)
            courseLevel = courseLevel.trim();
        return courseLevel;
    }

    @Override
    public String toString() {
        return "StudentCourse{" +
                "pen='" + pen + '\'' +
                ", courseCode='" + courseCode + '\'' +
                ", courseName='" + courseName + '\'' +
                ", originalCredits=" + originalCredits +
                ", courseLevel='" + courseLevel + '\'' +
                ", sessionDate='" + sessionDate + '\'' +
                ", customizedCourseName='" + customizedCourseName + '\'' +
                ", gradReqMet='" + gradReqMet + '\'' +
                ", gradReqMetDetail='" + gradReqMetDetail + '\'' +
                ", completedCoursePercentage=" + completedCoursePercentage +
                ", completedCourseLetterGrade='" + completedCourseLetterGrade + '\'' +
                ", interimPercent=" + interimPercent +
                ", interimLetterGrade='" + interimLetterGrade + '\'' +
                ", bestSchoolPercent=" + bestSchoolPercent +
                ", bestExamPercent=" + bestExamPercent +
                ", schoolPercent=" + schoolPercent +
                ", examPercent=" + examPercent +
                ", equivOrChallenge='" + equivOrChallenge + '\'' +
                ", fineArtsAppliedSkills='" + fineArtsAppliedSkills + '\'' +
                ", metLitNumRequirement='" + metLitNumRequirement + '\'' +
                ", credits=" + credits +
                ", creditsUsedForGrad=" + creditsUsedForGrad +
                ", relatedCourse='" + relatedCourse + '\'' +
                ", relatedCourseName='" + relatedCourseName + '\'' +
                ", relatedLevel='" + relatedLevel + '\'' +
                ", hasRelatedCourse='" + hasRelatedCourse + '\'' +
                ", isNotCompleted=" + isNotCompleted +
                ", genericCourseType='" + genericCourseType + '\'' +
                ", language='" + language + '\'' +
                ", workExpFlag='" + workExpFlag + '\'' +
                ", specialCase='" + specialCase + '\'' +
                ", toWriteFlag='" + toWriteFlag + '\'' +
                ", provExamCourse='" + provExamCourse + '\'' +
                ", isProjected=" + isProjected +
                ", isFailed=" + isFailed +
                ", isDuplicate=" + isDuplicate +
                ", isCareerPrep=" + isCareerPrep +
                ", isLocallyDeveloped=" + isLocallyDeveloped +
                ", isBoardAuthorityAuthorized=" + isBoardAuthorityAuthorized +
                ", isIndependentDirectedStudies=" + isIndependentDirectedStudies +
                ", isUsed=" + isUsed +
                ", isRestricted=" + isRestricted +
                ", isNotEligibleForElective=" + isNotEligibleForElective +
                ", isUsedInMatchRule=" + isUsedInMatchRule +
                ", isLessCreditCourse=" + isLessCreditCourse +
                ", isValidationCourse=" + isValidationCourse +
                ", isCutOffCourse=" + isCutOffCourse +
                ", isGrade10Course=" + isGrade10Course +
                ", leftOverCredits=" + leftOverCredits +
                '}';
    }
}
