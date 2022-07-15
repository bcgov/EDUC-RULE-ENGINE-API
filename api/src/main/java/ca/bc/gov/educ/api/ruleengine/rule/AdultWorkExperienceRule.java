package ca.bc.gov.educ.api.ruleengine.rule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.educ.api.ruleengine.dto.CourseRequirement;
import ca.bc.gov.educ.api.ruleengine.dto.GradRequirement;
import ca.bc.gov.educ.api.ruleengine.dto.ProgramRequirement;
import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import ca.bc.gov.educ.api.ruleengine.util.RuleEngineApiUtils;
import ca.bc.gov.educ.api.ruleengine.util.RuleProcessorRuleUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class AdultWorkExperienceRule implements Rule {

	private static Logger logger = LoggerFactory.getLogger(AdultWorkExperienceRule.class);

	@Autowired
	private RuleProcessorData ruleProcessorData;

	public RuleData fire() {
		logger.debug("Adult Work Experience Rule");
		
		if (ruleProcessorData.getStudentCourses().isEmpty()) {
			logger.warn("!!!Empty list sent to Adult Work Experience Rule for processing");
			return ruleProcessorData;
		}

		List<StudentCourse> studentCourses = RuleProcessorRuleUtils
				.getUniqueStudentCourses(ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());
		String gradDate = RuleProcessorRuleUtils.getGradDate(studentCourses);
		int diff = RuleEngineApiUtils.getDifferenceInMonths(gradDate, "2014-09-01");
		if(diff < 0) {
			return ruleProcessorData;
		}
		List<CourseRequirement> courseRequirements = ruleProcessorData.getCourseRequirements();
		
		logger.debug("Unique Courses: {}",studentCourses.size());
		List<ProgramRequirement> gradProgramRules = ruleProcessorData
				.getGradProgramRules().stream().filter(gpr -> "MWEX".compareTo(gpr.getProgramRequirementCode().getRequirementTypeCode().getReqTypeCode()) == 0
						&& "Y".compareTo(gpr.getProgramRequirementCode().getActiveRequirement()) == 0 && "C".compareTo(gpr.getProgramRequirementCode().getRequirementCategory()) == 0)
				.collect(Collectors.toList());

		List<StudentCourse> finalCourseList = new ArrayList<>();
		for (ProgramRequirement gradProgramRule : gradProgramRules) {
			int numberOfWExCourses = 0;
	        ListIterator<StudentCourse> courseIterator = studentCourses.listIterator();
	        ObjectMapper objectMapper = new ObjectMapper();
	        while (courseIterator.hasNext()) {
	            StudentCourse tempCourse = courseIterator.next();

	            logger.debug("Processing Course: Code= {} Level= {}",tempCourse.getCourseCode(),tempCourse.getCourseLevel());
	            logger.debug("Course Requirements size: {}",courseRequirements.size());

	            List<CourseRequirement> tempCourseRequirement = courseRequirements.stream()
	                    .filter(cr -> tempCourse.getCourseCode().compareTo(cr.getCourseCode()) == 0
	                            && tempCourse.getCourseLevel().compareTo(cr.getCourseLevel()) == 0)
	                    .collect(Collectors.toList());

	            logger.debug("Temp Course Requirement: {}",tempCourseRequirement);

	            ProgramRequirement tempProgramRule = null;

	            if (!tempCourseRequirement.isEmpty()) {
	                for(CourseRequirement cr:tempCourseRequirement) {
	                	if(tempProgramRule == null) {
		                	tempProgramRule = gradProgramRules.stream()
			                        .filter(pr -> pr.getProgramRequirementCode().getProReqCode().compareTo(cr.getRuleCode().getCourseRequirementCode()) == 0)
			                        .findAny()
			                        .orElse(null);
	                	}
	                }
	            }
	            
	            if(tempCourseRequirement != null && tempProgramRule != null) {
	            	numberOfWExCourses++;
	            	if(numberOfWExCourses > 1) {
	            		tempCourse.setNotEligibleForElective(true);
	            	}
	            }

	            try {
					StudentCourse tempSC = objectMapper.readValue(objectMapper.writeValueAsString(tempCourse), StudentCourse.class);
	                if (tempSC != null)
	                    finalCourseList.add(tempSC);
	            } catch (IOException e) {
	                logger.error("ERROR: {}",e.getMessage());
	            }
	        }
	        
	        if(numberOfWExCourses > 1L) {
	        	gradProgramRule.getProgramRequirementCode().setPassed(false);
				List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasons();

				if (nonGradReasons == null)
					nonGradReasons = new ArrayList<>();

				nonGradReasons.add(new GradRequirement(gradProgramRule.getProgramRequirementCode().getTraxReqNumber(), gradProgramRule.getProgramRequirementCode().getNotMetDesc(),gradProgramRule.getProgramRequirementCode().getProReqCode()));
				ruleProcessorData.setNonGradReasons(nonGradReasons);
	        }
		}
		ruleProcessorData.setStudentCourses(finalCourseList);
		return ruleProcessorData;
	}

	@Override
	public void setInputData(RuleData inputData) {
		ruleProcessorData = (RuleProcessorData) inputData;
		logger.info("AdultWorkExperienceRule: Rule Processor Data set.");
	}

}
