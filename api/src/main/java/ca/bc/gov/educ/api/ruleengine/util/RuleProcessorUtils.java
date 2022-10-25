package ca.bc.gov.educ.api.ruleengine.util;

import ca.bc.gov.educ.api.ruleengine.dto.StudentAssessment;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class RuleProcessorUtils {

    private RuleProcessorUtils() {}

    private static final Logger logger = LoggerFactory.getLogger(RuleProcessorUtils.class);

    /**
     * *
     * @param collection
     * @return boolean
     *  True - if list is empty
     *  False - If list is not empty
     */
    public static boolean isEmptyOrNull(Collection< ? > collection) {
        return (collection == null || collection.isEmpty());
    }

    /**
     * *
     * @param collection
     * @return boolean
     *      *  True - if list not empty
     *      *  False - If list is empty
     */
    public static boolean isNotEmptyOrNull(Collection < ? > collection) {
        return !isEmptyOrNull(collection);
    }
}
