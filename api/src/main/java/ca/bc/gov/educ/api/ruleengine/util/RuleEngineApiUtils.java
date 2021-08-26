package ca.bc.gov.educ.api.ruleengine.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.educ.api.ruleengine.dto.GradProgramRule;
import ca.bc.gov.educ.api.ruleengine.dto.OptionalProgramRequirement;
import ca.bc.gov.educ.api.ruleengine.dto.ProgramRequirement;
import ca.bc.gov.educ.api.ruleengine.dto.StudentAssessment;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;

public class RuleEngineApiUtils {

    private static final Logger logger = LoggerFactory.getLogger(RuleEngineApiUtils.class);
    private static final String ERROR_MSG = "Error : ";
    private static final String DATE_FORMAT = "yyyyMM";

    private RuleEngineApiUtils() {}

	public static String formatDate(Date date) {
        if (date == null)
            return null;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
        return simpleDateFormat.format(date);
    }

    public static String formatDate(Date date, String dateFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        return simpleDateFormat.format(date);
    }

    public static Date parseDate(String dateString) {
        if (dateString == null || "".compareTo(dateString) == 0)
            return null;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
        Date date = new Date();

        try {
            date = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            logger.info(ERROR_MSG+e.getMessage());
        }

        return date;
    }

    public static Date parseDate(String dateString, String dateFormat) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        Date date = new Date();

        try {
            date = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            logger.info(ERROR_MSG+e.getMessage());
        }

        return date;
    }

    public static String parseTraxDate(String sessionDate) {
        if (sessionDate == null)
            return null;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
        Date date = new Date();

        try {
            date = simpleDateFormat.parse(sessionDate);
            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return localDate.getYear() + "/" + String.format("%02d", localDate.getMonthValue());

        } catch (ParseException e) {
            logger.info(ERROR_MSG+e.getMessage());
            return null;
        }
    }
    
    public static Date parsingTraxDate(String sessionDate) {
    	 String actualSessionDate = sessionDate + "/01";
    	 Date temp = new Date();
		 Date sDate = null;
         try {
            temp = RuleEngineApiUtils.parseDate(actualSessionDate, "yyyy/MM/dd");
            String sDates = RuleEngineApiUtils.formatDate(temp, "yyyy-MM-dd");
            sDate = RuleEngineApiUtils.parseDate(sDates, "yyyy-MM-dd");
         } catch (ParseException pe) {
            logger.error("ERROR: " + pe.getMessage());
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
                LocalDate.parse(date1).withDayOfMonth(1),
                LocalDate.parse(date2).withDayOfMonth(1));
    	return diff.getDays();
    }
    
    public static List<StudentCourse> getClone(List<StudentCourse> listCourses) {
    	ObjectMapper mapper = new ObjectMapper();
		String json = "";

		try {
			json = mapper.writeValueAsString(listCourses);
			return mapper.readValue(json, new TypeReference<List<StudentCourse>>(){});
		} catch (JsonProcessingException e) {
			logger.info(ERROR_MSG+e.getMessage());
		}
		return Collections.emptyList();
		
    }
    
    public static List<StudentAssessment> getAssessmentClone(List<StudentAssessment> listAssessments) {
    	ObjectMapper mapper = new ObjectMapper();
		String json = "";

		try {
			json = mapper.writeValueAsString(listAssessments);
			return mapper.readValue(json, new TypeReference<List<StudentAssessment>>(){});
		} catch (JsonProcessingException e) {
			logger.info(ERROR_MSG+e.getMessage());
		}
		return Collections.emptyList();
		
    }
    
    public static List<ProgramRequirement> getCloneProgramRule(List<ProgramRequirement> gradProgramRulesMatch) {
    	ObjectMapper mapper = new ObjectMapper();
		String json = "";

		try {
			json = mapper.writeValueAsString(gradProgramRulesMatch);
			return mapper.readValue(json, new TypeReference<List<ProgramRequirement>>(){});
		} catch (JsonProcessingException e) {
			logger.info(ERROR_MSG+e.getMessage());
		}
		return Collections.emptyList();
		
    }
    public static List<OptionalProgramRequirement> getCloneSpecialProgramRule(List<OptionalProgramRequirement> rules) {
    	ObjectMapper mapper = new ObjectMapper();
		String json = "";

		try {
			json = mapper.writeValueAsString(rules);
			return mapper.readValue(json, new TypeReference<List<OptionalProgramRequirement>>(){});
		} catch (JsonProcessingException e) {
			logger.info(ERROR_MSG+e.getMessage());
		}
		return Collections.emptyList();
		
    }

}
