package ca.bc.gov.educ.api.ruleengine.util;

import ca.bc.gov.educ.api.ruleengine.controller.RuleEngineController;
import ca.bc.gov.educ.api.ruleengine.rule.Rule;
import ca.bc.gov.educ.api.ruleengine.struct.RuleProcessorData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class RuleEngineApiUtils {

    private static final Logger logger = LoggerFactory.getLogger(RuleEngineApiUtils.class);

    public static String formatDate (Date date) {
        if (date == null)
            return null;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");
        return simpleDateFormat.format(date);
    }

    public static String formatDate (Date date, String dateFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        return simpleDateFormat.format(date);
    }

    public static Date parseDate (String dateString) {
        if (dateString == null || "".compareTo(dateString) == 0)
            return null;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");
        Date date = new Date();

        try {
            date = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    public static Date parseDate (String dateString, String dateFormat) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        Date date = new Date();

        try {
            date = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }
    
    public static String parseTraxDate (String sessionDate) {
        if (sessionDate == null)
            return null;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");
        Date date = new Date();

        try {
            date = simpleDateFormat.parse(sessionDate);
            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return localDate.getYear() +"/"+ String.format("%02d", localDate.getMonthValue());
            
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }       
    }

    public static int getDifferenceInMonths(String date1, String date2) {
        Period diff = Period.between(
                LocalDate.parse(date1).withDayOfMonth(1),
                LocalDate.parse(date2).withDayOfMonth(1));

        return diff.getMonths();
    }

    public static Rule getRuleObject(String ruleImplementation, RuleProcessorData data) {
        Class<Rule> clazz;
        Rule rule = null;

        try {
            clazz = (Class<Rule>)Class.forName("ca.bc.gov.educ.api.ruleengine.rule." + ruleImplementation);
            rule = clazz.getDeclaredConstructor(RuleProcessorData.class).newInstance(data);
            System.out.println("Class Created: " + rule.getClass());
        }catch (Exception e) {
            logger.debug("ERROR: No Such Class: " + ruleImplementation);
            logger.debug("Message:" + e.getMessage());
        }

        return rule;
    }

    public static <T> T cloneObject(T input) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();

        T output = (T) objectMapper
                .readValue(objectMapper.writeValueAsString(input), input.getClass());

        return output;
    }
}
