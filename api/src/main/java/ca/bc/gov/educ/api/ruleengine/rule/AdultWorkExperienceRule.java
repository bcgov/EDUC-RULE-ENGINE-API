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
import ca.bc.gov.educ.api.ruleengine.dto.GradProgramRule;
import ca.bc.gov.educ.api.ruleengine.dto.GradRequirement;
import ca.bc.gov.educ.api.ruleengine.dto.RuleData;
import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
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
			logger.warn("!!!Empty list sent to Min Adult Courses Rule for processing");
			return ruleProcessorData;
		}

		List<StudentCourse> studentCourses = RuleProcessorRuleUtils
				.getUniqueStudentCourses(ruleProcessorData.getStudentCourses(), ruleProcessorData.isProjected());
		List<CourseRequirement> courseRequirements = ruleProcessorData.getCourseRequirements();
		
		logger.debug("Unique Courses: " + studentCourses.size());
		List<GradProgramRule> gradProgramRules = ruleProcessorData
				.getGradProgramRules().stream().filter(gpr -> "MWEX".compareTo(gpr.getRequirementType()) == 0
						&& "Y".compareTo(gpr.getIsActive()) == 0 && "C".compareTo(gpr.getRuleCategory()) == 0)
				.collect(Collectors.toList());

		logger.debug(gradProgramRules.toString());
		List<StudentCourse> finalCourseList = new ArrayList<>();
		for (GradProgramRule gradProgramRule : gradProgramRules) {
			
	        ListIterator<StudentCourse> courseIterator = studentCourses.listIterator();
	        StudentCourse tempSC;
	        ObjectMapper objectMapper = new ObjectMapper();
	        while (courseIterator.hasNext()) {
	            StudentCourse tempCourse = courseIterator.next();

	            logger.debug("Processing Course: Code=" + tempCourse.getCourseCode() + " Level=" + tempCourse.getCourseLevel());
	            logger.debug("Course Requirements size: " + courseRequirements.size());

	            List<CourseRequirement> tempCourseRequirement = courseRequirements.stream()
	                    .filter(cr -> tempCourse.getCourseCode().compareTo(cr.getCourseCode()) == 0
	                            && tempCourse.getCourseLevel().compareTo(cr.getCourseLevel()) == 0)
	                    .collect(Collectors.toList());

	            logger.debug("Temp Course Requirement: " + tempCourseRequirement);

	            GradProgramRule tempProgramRule = null;

	            if (!tempCourseRequirement.isEmpty()) {
	                for(CourseRequirement cr:tempCourseRequirement) {
	                	if(tempProgramRule == null) {
		                	tempProgramRule = gradProgramRules.stream()
			                        .filter(pr -> pr.getRuleCode().compareTo(cr.getRuleCode()) == 0)
			                        .findAny()
			                        .orElse(null);
	                	}
	                }
	            }
	            
	            if(tempCourseRequirement != null && tempProgramRule != null) {
	            	tempCourse.setWorkExpCourse(true);
	            }
	            tempSC = new StudentCourse();
	            try {
	                tempSC = objectMapper.readValue(objectMapper.writeValueAsString(tempCourse), StudentCourse.class);
	                if (tempSC != null)
	                    finalCourseList.add(tempSC);
	            } catch (IOException e) {
	                logger.error("ERROR:" + e.getMessage());
	            }
	        }
	        
	        long numberOfWExCourses = finalCourseList.stream().filter(sc -> sc.isWorkExpCourse()).count();
	        if(numberOfWExCourses > 1L) {
	        	gradProgramRule.setPassed(false);
	        	ruleProcessorData.setGraduated(false);

				List<GradRequirement> nonGradReasons = ruleProcessorData.getNonGradReasons();

				if (nonGradReasons == null)
					nonGradReasons = new ArrayList<>();

				nonGradReasons.add(new GradRequirement(gradProgramRule.getRuleCode(), gradProgramRule.getNotMetDesc()));
				ruleProcessorData.setNonGradReasons(nonGradReasons);
	        }else {
	        	gradProgramRule.setPassed(true);

				List<GradRequirement> reqsMet = ruleProcessorData.getRequirementsMet();

				if (reqsMet == null)
					reqsMet = new ArrayList<>();

				reqsMet.add(new GradRequirement(gradProgramRule.getRuleCode(), gradProgramRule.getRequirementName()));
				ruleProcessorData.setRequirementsMet(reqsMet);
	        }
		}
		ruleProcessorData.setStudentCourses(finalCourseList);
		ruleProcessorData.getStudentCourses().addAll(ruleProcessorData.getExcludedCourses());
		return ruleProcessorData;
	}

	@Override
	public void setInputData(RuleData inputData) {
		ruleProcessorData = (RuleProcessorData) inputData;
		logger.info("AdultWorkExperienceRule: Rule Processor Data set.");
	}

}
