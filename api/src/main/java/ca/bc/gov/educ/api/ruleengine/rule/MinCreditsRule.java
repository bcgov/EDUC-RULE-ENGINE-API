package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.struct.MinCreditRuleData;
import ca.bc.gov.educ.api.ruleengine.struct.ProgramRule;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
public class MinCreditsRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(MinCreditsRule.class);

    @Autowired
    private ProgramRule programRule;

    public boolean fire(ProgramRule programRule) {
        int totalCredits;
        int requiredCredits = Integer.parseInt(programRule.getRequiredCredits().trim());

        /*
        List<AchievementDto> achievements = (List<AchievementDto>)parameters;

        if (achievements == null || achievements.size() == 0)
            return false;

        if (programRule.getRequiredLevel() == 0) {
            totalCredits = achievements
                    .stream()
                    .filter(achievement -> !achievement.isDuplicate()
                            && !achievement.isFailed()
                    )
                    .mapToInt(achievement -> achievement.getCredits())
                    .sum();
        }
        else {
            totalCredits = achievements
                    .stream()
                    .filter(achievement -> !achievement.isDuplicate()
                            && !achievement.isFailed()
                            && achievement.getCourse().getCourseGradeLevel().startsWith(programRule.getRequiredLevel() + "")
                            )
                    .mapToInt(achievement -> achievement.getCredits())
                    .sum();
        }

        logger.debug("Min Credits -> Required:" + requiredCredits + " Has:" + totalCredits);
        return totalCredits >= requiredCredits;

         */
        return false;
    }

    public boolean fire(MinCreditRuleData data) {
        return false;
    }

    @Override
    public <T> boolean fire(T parameters) {
        return false;
    }
}
