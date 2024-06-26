package ca.bc.gov.educ.api.ruleengine.rule;

import java.util.Calendar;
import java.util.Date;

public abstract class BaseRule implements Rule {

    Date toLastDayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return cal.getTime();
    }

}
