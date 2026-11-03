package ca.bc.gov.educ.api.ruleengine.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeZoneRulesTest {

    @Test
    void shouldUseUpdatedBritishColumbiaTimeOnNovemberThird2026() {
        ZoneId britishColumbia = ZoneId.of(RuleEngineApiConstants.BC_TIME_ZONE);
        ZonedDateTime result = Instant.parse("2026-11-03T07:30:00Z").atZone(britishColumbia);

        assertEquals(ZoneOffset.ofHours(-7), result.getOffset());
        assertEquals(LocalDate.of(2026, 11, 3), result.toLocalDate());
        assertEquals(0, result.getHour());
        assertEquals(30, result.getMinute());
    }
}
