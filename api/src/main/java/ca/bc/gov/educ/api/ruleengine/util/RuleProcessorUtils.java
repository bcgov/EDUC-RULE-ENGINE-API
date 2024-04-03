package ca.bc.gov.educ.api.ruleengine.util;

import java.util.Collection;

public class RuleProcessorUtils {

    private RuleProcessorUtils() {}

    /**
     * *
     * @return boolean
     *  True - if list is empty
     *  False - If list is not empty
     */
    public static boolean isEmptyOrNull(Collection< ? > collection) {
        return (collection == null || collection.isEmpty());
    }

    /**
     * *
     * @return boolean
     *      *  True - if list not empty
     *      *  False - If list is empty
     */
    public static boolean isNotEmptyOrNull(Collection < ? > collection) {
        return !isEmptyOrNull(collection);
    }

    /**
     * *
     * @return boolean
     *      *  False - if String not empty
     *      *  True - If String is empty
     */
    public static boolean isEmptyOrNull(String str) {
        return (str == null || str.isEmpty());
    }
}
