package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class MainProgramCompleteRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(MainProgramCompleteRule.class);

    @Autowired
    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {

		Map<String, OptionalProgramRuleProcessor> mapOptional = ruleProcessorData.getMapOptional();
		List<GradRequirement> nongReason = ruleProcessorData.getNonGradReasons();
		for (Map.Entry<String, OptionalProgramRuleProcessor> entry : mapOptional.entrySet()) {
			OptionalProgramRuleProcessor obj = entry.getValue();
			if(obj != null && obj.isHasOptionalProgram() && obj.isOptionalProgramGraduated()) {
				processMainProgramCompleteRules(nongReason,obj);
			}
		}
		ruleProcessorData.setMapOptional(mapOptional);
		return ruleProcessorData;
    }

	private void processMainProgramCompleteRules(List<GradRequirement> nonGradReasons,OptionalProgramRuleProcessor obj) {
		List<OptionalProgramRequirement> optionalProgramNoRule = obj.getOptionalProgramRules()
				.stream()
				.filter(gradOptionalProgramRule -> "SR".compareTo(gradOptionalProgramRule.getOptionalProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
						&& "Y".compareTo(gradOptionalProgramRule.getOptionalProgramRequirementCode().getActiveRequirement()) == 0)
				.collect(Collectors.toList());
		for(OptionalProgramRequirement opReq:optionalProgramNoRule) {
			if(opReq.getOptionalProgramRequirementCode().getOptProReqCode().compareTo("957")==0) {
				if (nonGradReasons == null || nonGradReasons.isEmpty()) {
					logger.debug("{} Passed", opReq.getOptionalProgramRequirementCode().getLabel());
					opReq.getOptionalProgramRequirementCode().setPassed(true);
				} else {
					logger.debug("{} Failed", opReq.getOptionalProgramRequirementCode().getLabel());
					opReq.getOptionalProgramRequirementCode().setPassed(false);
				}
				List<OptionalProgramRequirement> failedRules = obj.getOptionalProgramRules().stream()
						.filter(pr -> !pr.getOptionalProgramRequirementCode().isPassed()).collect(Collectors.toList());

				if (failedRules.isEmpty()) {
					logger.debug("All the match rules met!");
					List<GradRequirement> resMet = obj.getRequirementsMetOptionalProgram();

					if (resMet == null)
						resMet = new ArrayList<>();

					resMet.add(new GradRequirement(opReq.getOptionalProgramRequirementCode().getOptProReqCode(), opReq.getOptionalProgramRequirementCode().getLabel(),opReq.getOptionalProgramRequirementCode().getOptProReqCode()));
					obj.setRequirementsMetOptionalProgram(resMet);
				} else {
					List<GradRequirement> requirementsNotMet = new ArrayList<>();
					for (OptionalProgramRequirement failedRule : failedRules) {
						requirementsNotMet.add(new GradRequirement(failedRule.getOptionalProgramRequirementCode().getOptProReqCode(), failedRule.getOptionalProgramRequirementCode().getNotMetDesc(),failedRule.getOptionalProgramRequirementCode().getOptProReqCode()));
					}
					obj.setOptionalProgramGraduated(false);
					List<GradRequirement> nonGReasons = obj.getNonGradReasonsOptionalProgram();

					if (nonGReasons == null)
						nonGReasons = new ArrayList<>();

					nonGReasons.addAll(requirementsNotMet);
					obj.setNonGradReasonsOptionalProgram(nonGReasons);
				}
			}
		}
	}
    @Override
    public void setInputData(RuleData inputData) {
        ruleProcessorData = (RuleProcessorData) inputData;
        logger.debug("MainProgramCompleteRule: Rule Processor Data set.");
    }
}
