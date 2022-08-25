package ca.bc.gov.educ.api.ruleengine.rule;

import ca.bc.gov.educ.api.ruleengine.dto.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class OptionalProgramNoRule implements Rule {

    private static Logger logger = LoggerFactory.getLogger(OptionalProgramNoRule.class);

    private RuleProcessorData ruleProcessorData;

    @Override
    public RuleData fire() {

		Map<String, OptionalProgramRuleProcessor> mapOptional = ruleProcessorData.getMapOptional();
		OptionalProgramRuleProcessor obj;
		String program = ruleProcessorData.getGradProgram().getProgramCode();
		if(program.compareTo("1996-PF")==0 && mapOptional.get("DD") != null) {
			obj = mapOptional.get("DD");
			processOptionalProgramNoRules("DD",obj,mapOptional);
		}else if(program.compareTo("2018-EN")==0 || program.compareTo("2004-EN")==0 || program.compareTo("2018-PF")==0 ||program.compareTo("2004-PF")==0 || program.compareTo("1996-EN")==0) {
			if(mapOptional.get("AD") != null) {
				obj = mapOptional.get("AD");
				processOptionalProgramNoRules("AD",obj,mapOptional);
			}
			if(mapOptional.get("BC") != null) {
				obj = mapOptional.get("BC");
				processOptionalProgramNoRules("BC",obj,mapOptional);
			}
			if(mapOptional.get("BD") != null) {
				obj = mapOptional.get("BD");
				processOptionalProgramNoRules("BD",obj,mapOptional);
			}
		}else if(program.compareTo("SCCP")==0) {
			if (mapOptional.get("FR") != null) {
				obj = mapOptional.get("FR");
				processOptionalProgramNoRules("FR", obj, mapOptional);
			}
			if (mapOptional.get("CP") != null) {
				obj = mapOptional.get("CP");
				processOptionalProgramNoRules("CP", obj, mapOptional);
			}

		}
		else if(program.compareTo("1950")==0) {
			if(mapOptional.get("AD") != null) {
				obj = mapOptional.get("AD");
				processOptionalProgramNoRules("AD",obj,mapOptional);
			}
			if(mapOptional.get("BC") != null) {
				obj = mapOptional.get("BC");
				processOptionalProgramNoRules("BC",obj,mapOptional);
			}
			if(mapOptional.get("BD") != null) {
				obj = mapOptional.get("BD");
				processOptionalProgramNoRules("BD",obj,mapOptional);
			}
			if(mapOptional.get("CP") != null) {
				obj = mapOptional.get("CP");
				processOptionalProgramNoRules("CP",obj,mapOptional);
			}
		}
		ruleProcessorData.setMapOptional(mapOptional);
		return ruleProcessorData;
    }

	private void processOptionalProgramNoRules(String opPrgCode,OptionalProgramRuleProcessor obj,Map<String,OptionalProgramRuleProcessor> mapOptional) {
		List<OptionalProgramRequirement> optionalProgramNoRule = obj.getOptionalProgramRules()
				.stream()
				.filter(gradOptionalProgramRule -> "SR".compareTo(gradOptionalProgramRule.getOptionalProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
						&& "Y".compareTo(gradOptionalProgramRule.getOptionalProgramRequirementCode().getActiveRequirement()) == 0)
				.collect(Collectors.toList());
		for (OptionalProgramRequirement opReq : optionalProgramNoRule) {
			if (opReq.getOptionalProgramRequirementCode().getOptProReqCode().compareTo("957") != 0) {
				logger.debug("{} Passed", opReq.getOptionalProgramRequirementCode().getLabel());
				if (obj.isHasOptionalProgram()) {
					opReq.getOptionalProgramRequirementCode().setPassed(true);
					List<GradRequirement> resMet = obj.getRequirementsMetOptionalProgram();

					if (resMet == null)
						resMet = new ArrayList<>();
					resMet.add(new GradRequirement(opReq.getOptionalProgramRequirementCode().getOptProReqCode(), opReq.getOptionalProgramRequirementCode().getLabel(), opReq.getOptionalProgramRequirementCode().getOptProReqCode()));
					obj.setRequirementsMetOptionalProgram(resMet);
				}
				mapOptional.put(opPrgCode, obj);
			}
		}
	}

}
