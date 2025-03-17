package ca.bc.gov.educ.api.ruleengine.util;

import ca.bc.gov.educ.api.ruleengine.dto.OptionalProgramRequirement;
import ca.bc.gov.educ.api.ruleengine.dto.ProgramRequirement;
import ca.bc.gov.educ.api.ruleengine.dto.StudentAssessment;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class RuleEngineApiUtils {

    private static final Logger logger = LoggerFactory.getLogger(RuleEngineApiUtils.class);
    private static final String ERROR_MSG = "Error : {}";

    private RuleEngineApiUtils() {}

	public static String formatDate(Date date, String dateFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        return simpleDateFormat.format(date);
    }

    public static Date parseDate(String dateString, String dateFormat) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        Date date = new Date();

        try {
            date = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            logger.error(ERROR_MSG,e.getMessage());
        }

        return date;
    }

    public static Date toDate(LocalDate localDate) {
        if(localDate == null) return null;
        return Date.from(localDate.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }
    
    public static Date parsingTraxDate(String sessionDate) {
    	 String actualSessionDate = sessionDate + "/01";
    	 Date temp;
		 Date sDate = null;
         try {
            temp = toLastDayOfMonth(RuleEngineApiUtils.parseDate(actualSessionDate, RuleEngineApiConstants.DATE_FORMAT));
            String sDates = RuleEngineApiUtils.formatDate(temp, RuleEngineApiConstants.DATE_FORMAT);
            sDate = RuleEngineApiUtils.parseDate(sDates, RuleEngineApiConstants.DATE_FORMAT);
         } catch (ParseException pe) {
            logger.error(ERROR_MSG,pe.getMessage());
         }
         return sDate;
    }

    public static int getDifferenceInMonths(String date1, String date2) {
    	Period diff = Period.between(
                LocalDate.parse(date1).withDayOfMonth(1),
                LocalDate.parse(date2).withDayOfMonth(1));
    	int monthsYear = diff.getYears() * 12;
    	int months = diff.getMonths();
    	
    	

        return monthsYear + months;
    }
    
    public static int getDifferenceInDays(String date1, String date2) {
        Period diff = Period.between(
                LocalDate.parse(date1),
                LocalDate.parse(date2));
    	return diff.getDays() + diff.getMonths()*30;
    }
    
    public static List<StudentCourse> getClone(List<StudentCourse> listCourses) {
    	ObjectMapper mapper = new ObjectMapper();
		try {
			String json = mapper.writeValueAsString(listCourses);
			return mapper.readValue(json, new TypeReference<>(){});
		} catch (JsonProcessingException e) {
			logger.error(ERROR_MSG,e.getMessage());
		}
		return Collections.emptyList();
		
    }
    
    public static List<StudentAssessment> getAssessmentClone(List<StudentAssessment> listAssessments) {
    	ObjectMapper mapper = new ObjectMapper();
		try {
			String json = mapper.writeValueAsString(listAssessments);
			return mapper.readValue(json, new TypeReference<>(){});
		} catch (JsonProcessingException e) {
			logger.error(ERROR_MSG,e.getMessage());
		}
		return Collections.emptyList();
		
    }
    
    public static List<ProgramRequirement> getCloneProgramRule(List<ProgramRequirement> gradProgramRulesMatch) {
    	ObjectMapper mapper = new ObjectMapper();

		try {
			String json = mapper.writeValueAsString(gradProgramRulesMatch);
			return mapper.readValue(json, new TypeReference<>(){});
		} catch (JsonProcessingException e) {
			logger.error(ERROR_MSG,e.getMessage());
		}
		return Collections.emptyList();
		
    }

    public static List<ProgramRequirement> getMatchProgramRules(List<ProgramRequirement> gradProgramRules) {
        if (gradProgramRules.isEmpty()) {
            return new ArrayList<>();
        }
        return gradProgramRules.stream()
            .filter(gradProgramRule -> "M".compareTo(gradProgramRule.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
                    && "Y".compareTo(gradProgramRule.getProgramRequirementCode().getActiveRequirement()) == 0
                    && "C".compareTo(gradProgramRule.getProgramRequirementCode().getRequirementCategory()) == 0)
            .collect(Collectors.toList());
    }

    public static List<OptionalProgramRequirement> getCloneOptionalProgramRule(List<OptionalProgramRequirement> rules) {
    	ObjectMapper mapper = new ObjectMapper();
		try {
			String json = mapper.writeValueAsString(rules);
			return mapper.readValue(json, new TypeReference<>(){});
		} catch (JsonProcessingException e) {
			logger.error(ERROR_MSG,e.getMessage());
		}
		return Collections.emptyList();
		
    }

    public static boolean checkDateForRestrictedCourses(String startDate,String endDate,String currentSessionDate) {
        try {
            Date sDate = toLastDayOfMonth(parseDate(startDate+"/01",RuleEngineApiConstants.DATE_FORMAT));
            if(endDate != null) {
                Date eDate = toLastDayOfMonth(parseDate(endDate + "/01", RuleEngineApiConstants.DATE_FORMAT));
                return toLastDayOfMonth(parseDate(currentSessionDate + "/01", RuleEngineApiConstants.DATE_FORMAT)).after(sDate) && toLastDayOfMonth(parseDate(currentSessionDate + "/01", "yyyy/MM/dd")).before(eDate);
            }else {
                return toLastDayOfMonth(parseDate(currentSessionDate + "/01", RuleEngineApiConstants.DATE_FORMAT)).after(sDate);
            }
        } catch (ParseException e) {
            logger.error(ERROR_MSG,e.getMessage());
        }
        return false;
    }

    public static boolean compareCourseSessionDates(String sessionDate1,String sessionDate2) {
        String today = RuleEngineApiUtils.formatDate(new Date(), "yyyy-MM-dd");
        sessionDate1 = sessionDate1 + "/01";
        sessionDate2 = sessionDate2 + "/01";

        try {
            Date temp1 = toLastDayOfMonth(RuleEngineApiUtils.parseDate(sessionDate1, "yyyy/MM/dd"));
            sessionDate1 = RuleEngineApiUtils.formatDate(temp1, "yyyy-MM-dd");
            Date temp2 = toLastDayOfMonth(RuleEngineApiUtils.parseDate(sessionDate2, "yyyy/MM/dd"));
            sessionDate2 = RuleEngineApiUtils.formatDate(temp2, "yyyy-MM-dd");
        } catch (ParseException pe) {
            logger.error("ERROR: {}",pe.getMessage());
        }

        int diff1 = RuleEngineApiUtils.getDifferenceInMonths(sessionDate1,today);
        int diff2 = RuleEngineApiUtils.getDifferenceInMonths(sessionDate2,today);
        if(diff1 < diff2) {
            return true;
        }else{
            return false;
        }
    }

    // Courses with both finalLG(Letter Grade) & finalPercentage have some values
    public static boolean isCompletedCourse(String finalGrade, Double finalPercentage) {
        if (finalGrade != null && finalPercentage != null) {
            return !"".equalsIgnoreCase(finalGrade.trim()) && finalPercentage.compareTo(0.0D) > 0;
        }
        return false;
    }

    static Date toLastDayOfMonth(Date date) {
        if(date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            return cal.getTime();
        }
        return null;
    }
}
