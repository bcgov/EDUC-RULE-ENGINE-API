package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.OptionalProgramRuleProcessor;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.SpecialCase;
import ca.bc.gov.educ.api.ruleengine.dto.StudentAssessment;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FailedAssessmentsRuleTest {

    @Test
    public void shouldMarkDualDogwoodAssessmentsFailedForNonPassingSpecialCases() {
        assertAssessmentFailsForMainAndOptionalProgram("DD", "LTE10", "Q");
        assertAssessmentFailsForMainAndOptionalProgram("DD", "LTE12", "X");
    }

    @Test
    public void shouldMarkFrenchImmersionAssessmentFailedForNonPassingSpecialCases() {
        assertAssessmentFailsForMainAndOptionalProgram("FI", "LTF12", "Q");
        assertAssessmentFailsForMainAndOptionalProgram("FI", "LTF12", "X");
    }

    @Test
    public void shouldNotMarkPassingSpecialCaseFailed() {
        RuleProcessorData result = processAssessment("FI", "LTF12", "M");

        assertFalse(result.getStudentAssessments().get(0).isFailed());
        assertFalse(result.getMapOptional().get("FI").getStudentAssessmentsOptionalProgram().get(0).isFailed());
    }

    private void assertAssessmentFailsForMainAndOptionalProgram(String optionalProgramCode, String assessmentCode, String specialCase) {
        RuleProcessorData result = processAssessment(optionalProgramCode, assessmentCode, specialCase);

        assertTrue(result.getStudentAssessments().get(0).isFailed());
        assertTrue(result.getMapOptional().get(optionalProgramCode).getStudentAssessmentsOptionalProgram().get(0).isFailed());
        assertTrue(RuleProcessorRuleUtils.getUniqueStudentAssessments(
                result.getMapOptional().get(optionalProgramCode).getStudentAssessmentsOptionalProgram(), false).isEmpty());
    }

    private RuleProcessorData processAssessment(String optionalProgramCode, String assessmentCode, String specialCase) {
        StudentAssessment mainAssessment = createAssessment(assessmentCode, specialCase);
        StudentAssessment optionalAssessment = createAssessment(assessmentCode, specialCase);

        OptionalProgramRuleProcessor optionalProgram = new OptionalProgramRuleProcessor();
        optionalProgram.setStudentAssessmentsOptionalProgram(new ArrayList<>(List.of(optionalAssessment)));

        Map<String, OptionalProgramRuleProcessor> optionalPrograms = new HashMap<>();
        optionalPrograms.put(optionalProgramCode, optionalProgram);

        RuleProcessorData ruleProcessorData = new RuleProcessorData();
        ruleProcessorData.setStudentAssessments(new ArrayList<>(List.of(mainAssessment)));
        ruleProcessorData.setExcludedAssessments(new ArrayList<>());
        ruleProcessorData.setSpecialCaseList(createSpecialCases());
        ruleProcessorData.setMapOptional(optionalPrograms);
        ruleProcessorData.setProjected(false);

        return (RuleProcessorData) new FailedAssessmentsRule(ruleProcessorData).fire();
    }

    private StudentAssessment createAssessment(String assessmentCode, String specialCase) {
        StudentAssessment assessment = new StudentAssessment();
        assessment.setPen("123456789");
        assessment.setAssessmentCode(assessmentCode);
        assessment.setSessionDate("2026/06");
        assessment.setSpecialCase(specialCase);
        return assessment;
    }

    private List<SpecialCase> createSpecialCases() {
        return List.of(
                createSpecialCase("Q", "N"),
                createSpecialCase("X", "N"),
                createSpecialCase("M", "Y")
        );
    }

    private SpecialCase createSpecialCase(String code, String passFlag) {
        SpecialCase specialCase = new SpecialCase();
        specialCase.setSpCase(code);
        specialCase.setPassFlag(passFlag);
        return specialCase;
    }
}
