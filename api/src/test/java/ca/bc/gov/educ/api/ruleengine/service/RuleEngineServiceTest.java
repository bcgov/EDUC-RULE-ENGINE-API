package ca.bc.gov.educ.api.ruleengine.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import ca.bc.gov.educ.api.ruleengine.dto.StudentCourse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class RuleEngineServiceTest {

	@Autowired RuleEngineService ruleEngineService;

	private Map<String,Error> errors = new HashMap<>();
	private static Logger logger = Logger.getLogger(RuleEngineServiceTest.class.getName());
	
	@Test
	public void testProcessGradAlgorithmRules() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("2018-EN");	
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_2018EN_FI() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("FI");	
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_2018PF_DD() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("DD");	
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_1950EN() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("1950-EN");	
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_1950EN_2() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("1950-EN-2");	
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_1950EN_3() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("1950-EN-3");	
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_1950EN_4() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("1950-EN-4");	
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_2018EN_CP() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("CP");	
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_2018PF() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("2018-PF");	
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_2018EN() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("2018-EN-UNGRAD");	
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_2018EN_Dup_Asmt() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("2018-EN-DUP-ASSMT");	
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_2018PF_DD_Fail() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("DD-FAIL");	
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_2018EN_MINCREDITS_Fail() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("2018-EN-MIN-CREDIT-FAIL");	
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	//
	@Test
	public void testProcessGradAlgorithmRules_projectedfalse() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("2018-EN");
		assert ruleProcessorData != null;
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_2018EN_FI_projectedfalse() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("FI");
		assert ruleProcessorData != null;
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_2018PF_DD_projectedfalse() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("DD");
		assert ruleProcessorData != null;
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_1950EN_projectedfalse() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("1950-EN");
		assert ruleProcessorData != null;
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_1950EN_2_projectedfalse() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("1950-EN-2");
		assert ruleProcessorData != null;
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_1950EN_3_projectedfalse() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("1950-EN-3");
		assert ruleProcessorData != null;
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_1950EN_4_projectedfalse() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("1950-EN-4");
		assert ruleProcessorData != null;
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_2018EN_CP_projectedfalse() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("CP");
		assert ruleProcessorData != null;
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_2018PF_projectedfalse() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("2018-PF");
		assert ruleProcessorData != null;
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_2018EN_projectedfalse() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("2018-EN-UNGRAD");
		assert ruleProcessorData != null;
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_2018EN_Dup_Asmt_projectedfalse() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("2018-EN-DUP-ASSMT");
		assert ruleProcessorData != null;
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_2018PF_DD_Fail_projectedfalse() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("DD-FAIL");
		assert ruleProcessorData != null;
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_2018EN_MINCREDITS_Fail_projectedfalse() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("2018-EN-MIN-CREDIT-FAIL");
		assert ruleProcessorData != null;
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}

	@Test
	public void testProcessGradAlgorithmRules_1996EN_106945306() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("1996-EN-106945306");
		assert ruleProcessorData != null;
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);

		List<StudentCourse> stdList = ruleProcessorData.getStudentCourses();
		for (StudentCourse studentCourse : stdList) {
			if (!studentCourse.isDuplicate() && !studentCourse.isFailed() && !studentCourse.isNotCompleted()
					&& !studentCourse.isProjected() && !studentCourse.isLessCreditCourse() && studentCourse.isUsed()
					&& !studentCourse.isCutOffCourse() && !studentCourse.isValidationCourse() && !studentCourse.isGrade10Course()) {

				if (studentCourse.getCourseCode().compareTo("XPTU") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("XPTU11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("XPTU") == 0 && studentCourse.getCourseLevel().compareTo("11A") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("XPTU11A", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("FN") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("FN11730", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("MAA") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("MAA11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("IMA") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("724"));
					} catch (AssertionError e) {
						errors.put("IMA11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("CAPP") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("728"));
					} catch (AssertionError e) {
						errors.put("CAPP11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("MA") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("MA11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("COM") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("721"));
					} catch (AssertionError e) {
						errors.put("COM11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("CH") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("725"));
					} catch (AssertionError e) {
						errors.put("CH11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("SS") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("723"));
					} catch (AssertionError e) {
						errors.put("SS11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("BI") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("BI11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("CAPP") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("729"));
					} catch (AssertionError e) {
						errors.put("CAPP12", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("TEX") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("732"));
					} catch (AssertionError e) {
						errors.put("TEX12", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("FM") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("720"));
					} catch (AssertionError e) {
						errors.put("FM12", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("COM") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("722"));
					} catch (AssertionError e) {
						errors.put("COM12", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("EN") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("720"));
					} catch (AssertionError e) {
						errors.put("EN12", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("CH") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("720"));
					} catch (AssertionError e) {
						errors.put("CH12", e);
					}

				}
			}
		}
		errors.forEach((k,v)-> {
			logger.info("Course : "+k+", Error : "+v);
		});

		assertTrue(ruleProcessorData.isGraduated());
		assertTrue(errors.size()==0);
	}

	@Test
	public void testProcessGradAlgorithmRules_1996EN_104337712() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("1996-EN-104337712");
		assert ruleProcessorData != null;
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);

		List<StudentCourse> stdList = ruleProcessorData.getStudentCourses();
		for (StudentCourse studentCourse : stdList) {
			if (!studentCourse.isDuplicate() && !studentCourse.isFailed() && !studentCourse.isNotCompleted()
					&& !studentCourse.isProjected() && !studentCourse.isLessCreditCourse() && studentCourse.isUsed()
					&& !studentCourse.isCutOffCourse() && !studentCourse.isValidationCourse() && !studentCourse.isGrade10Course()) {

				if (studentCourse.getCourseCode().compareTo("CAPP") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("728"));
					} catch (AssertionError e) {
						errors.put("CAPP11", e);
					}

				}
				if (studentCourse.getCourseCode().compareTo("PE") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("PE11", e);
					}

				}
				if (studentCourse.getCourseCode().compareTo("XTA") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("XTA11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("XCRCU") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("XCRCU11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("SS") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("723"));
					} catch (AssertionError e) {
						errors.put("SS11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("FR") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("FR11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("TEX") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("732"));
					} catch (AssertionError e) {
						errors.put("TEX11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("EN") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("721"));
					} catch (AssertionError e) {
						errors.put("EN11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("BI") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("725"));
					} catch (AssertionError e) {
						errors.put("BI11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("CH") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("CH11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("MA") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("724"));
					} catch (AssertionError e) {
						errors.put("MA11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("PE") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("PE12730", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("PE") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("720"));
					} catch (AssertionError e) {
						errors.put("PE12720", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("CAPP") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("729"));
					} catch (AssertionError e) {
						errors.put("CAPP12", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("LAW") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("720"));
					} catch (AssertionError e) {
						errors.put("LAW12", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("EN") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("722"));
					} catch (AssertionError e) {
						errors.put("EN12", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("BI") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("720"));
					} catch (AssertionError e) {
						errors.put("BI12", e);
					}

				}
			}
		}
		errors.forEach((k,v)-> {
			logger.info("Course : "+k+", Error : "+v);
		});

		assertTrue(ruleProcessorData.isGraduated());
		assertTrue(errors.size()==0);
	}

	@Test
	public void testProcessGradAlgorithmRules_1996EN_101821056() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("1996-EN-101821056");
		assert ruleProcessorData != null;
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);

		List<StudentCourse> stdList = ruleProcessorData.getStudentCourses();
		for (StudentCourse studentCourse : stdList) {
			if (!studentCourse.isDuplicate() && !studentCourse.isFailed() && !studentCourse.isNotCompleted()
					&& !studentCourse.isProjected() && !studentCourse.isLessCreditCourse() && studentCourse.isUsed()
					&& !studentCourse.isCutOffCourse() && !studentCourse.isValidationCourse() && !studentCourse.isGrade10Course()) {

				if (studentCourse.getCourseCode().compareTo("TEX") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("732"));
					} catch (AssertionError e) {
						errors.put("TEX11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("EN") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("721"));
					} catch (AssertionError e) {
						errors.put("EN11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("VAMT") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("VAMT11", e);
					}

				}
				if (studentCourse.getCourseCode().compareTo("SS") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("723"));
					} catch (AssertionError e) {
						errors.put("SS11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("CH") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("725"));
					} catch (AssertionError e) {
						errors.put("CH11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("BI") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("BI11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("IMA") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("724"));
					} catch (AssertionError e) {
						errors.put("IMA11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("CAPP") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("728"));
					} catch (AssertionError e) {
						errors.put("CAPP11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("CPPSY") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("CPPSY11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("FM") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("FM12730", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("FM") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("720"));
					} catch (AssertionError e) {
						errors.put("FM12720", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("CPPTU") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("CPPTU12730", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("CPPTU") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("720"));
					} catch (AssertionError e) {
						errors.put("CPPTU12720", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("CCN") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("CCN12730", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("CCN") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("720"));
					} catch (AssertionError e) {
						errors.put("CCN12720", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("EN") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("722"));
					} catch (AssertionError e) {
						errors.put("EN12", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("CAPP") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("729"));
					} catch (AssertionError e) {
						errors.put("CAPP12", e);
					}

				}
			}
		}
		errors.forEach((k,v)-> {
			logger.info("Course : "+k+", Error : "+v);
		});

		assertTrue(ruleProcessorData.isGraduated());
		assertTrue(errors.size()==0);
	}

	@Test
	public void testProcessGradAlgorithmRules_1996EN_101541068() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("1996-EN-101541068");
		assert ruleProcessorData != null;
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);

		List<StudentCourse> stdList = ruleProcessorData.getStudentCourses();
		for (StudentCourse studentCourse : stdList) {
			if (!studentCourse.isDuplicate() && !studentCourse.isFailed() && !studentCourse.isNotCompleted()
					&& !studentCourse.isProjected() && !studentCourse.isLessCreditCourse() && studentCourse.isUsed()
					&& !studentCourse.isCutOffCourse() && !studentCourse.isValidationCourse() && !studentCourse.isGrade10Course()) {

				if (studentCourse.getCourseCode().compareTo("XPT") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("XPT11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("CAPP") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("728"));
					} catch (AssertionError e) {
						errors.put("CAPP11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("SS") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("723"));
					} catch (AssertionError e) {
						errors.put("SS11", e);
					}

				}
				if (studentCourse.getCourseCode().compareTo("EN") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("721"));
					} catch (AssertionError e) {
						errors.put("EN11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("FR") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("FR11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("BI") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("725"));
					} catch (AssertionError e) {
						errors.put("BI11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("CH") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("CH11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("MA") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("724"));
					} catch (AssertionError e) {
						errors.put("MA11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("CAPP") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("729"));
					} catch (AssertionError e) {
						errors.put("CAPP12", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("LAW") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("720"));
					} catch (AssertionError e) {
						errors.put("LAW12720", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("LAW") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("LAW12730", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("TEX") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("732"));
					} catch (AssertionError e) {
						errors.put("TEX12", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("EN") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("722"));
					} catch (AssertionError e) {
						errors.put("EN12", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("GEO") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("GEO12730", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("GEO") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("720"));
					} catch (AssertionError e) {
						errors.put("GEO12720", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("BI") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("720"));
					} catch (AssertionError e) {
						errors.put("BI12720", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("BI") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("BI12730", e);
					}

				}
			}
		}
		errors.forEach((k,v)-> {
			logger.info("Course : "+k+", Error : "+v);
		});

		assertTrue(ruleProcessorData.isGraduated());
		assertTrue(errors.size()==0);
	}

	@Test
	public void testProcessGradAlgorithmRules_1996EN_101171718() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("1996-EN-101171718");
		assert ruleProcessorData != null;
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);

		List<StudentCourse> stdList = ruleProcessorData.getStudentCourses();
		for (StudentCourse studentCourse : stdList) {
			if (!studentCourse.isDuplicate() && !studentCourse.isFailed() && !studentCourse.isNotCompleted()
					&& !studentCourse.isProjected() && !studentCourse.isLessCreditCourse() && studentCourse.isUsed()
					&& !studentCourse.isCutOffCourse() && !studentCourse.isValidationCourse() && !studentCourse.isGrade10Course()) {

				if (studentCourse.getCourseCode().compareTo("CAPP") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("728"));
					} catch (AssertionError e) {
						errors.put("CAPP11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("AR") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("AR11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("EN") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("721"));
					} catch (AssertionError e) {
						errors.put("EN11", e);
					}

				}
				if (studentCourse.getCourseCode().compareTo("BI") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("725"));
					} catch (AssertionError e) {
						errors.put("BI11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("CPCAN") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("732"));
					} catch (AssertionError e) {
						errors.put("CPCAN11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("SS") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("723"));
					} catch (AssertionError e) {
						errors.put("SS11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("FR") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("FR11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("IMA") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("724"));
					} catch (AssertionError e) {
						errors.put("IMA", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("CPHSC") == 0 && studentCourse.getCourseLevel().compareTo("11A") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("CPHSC11A", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("CPGC") == 0 && studentCourse.getCourseLevel().compareTo("11A") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("CPGC11A", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("FM") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("FM12730", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("FM") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("720"));
					} catch (AssertionError e) {
						errors.put("FM12720", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("CAPP") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("729"));
					} catch (AssertionError e) {
						errors.put("CAPP12", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("LAW") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("LAW12730", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("LAW") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("720"));
					} catch (AssertionError e) {
						errors.put("LAW12720", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("EN") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("722"));
					} catch (AssertionError e) {
						errors.put("EN12", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("FR") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("720"));
					} catch (AssertionError e) {
						errors.put("FR12", e);
					}

				}
			}
		}
		errors.forEach((k,v)-> {
			logger.info("Course : "+k+", Error : "+v);
		});

		assertTrue(ruleProcessorData.isGraduated());
		assertTrue(errors.size()==0);
	}

	@Test
	public void testProcessGradAlgorithmRules_1996EN_101039378() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("1996-EN-101039378");
		assert ruleProcessorData != null;
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);

		List<StudentCourse> stdList = ruleProcessorData.getStudentCourses();
		for (StudentCourse studentCourse : stdList) {
			if (!studentCourse.isDuplicate() && !studentCourse.isFailed() && !studentCourse.isNotCompleted()
					&& !studentCourse.isProjected() && !studentCourse.isLessCreditCourse() && studentCourse.isUsed()
					&& !studentCourse.isCutOffCourse() && !studentCourse.isValidationCourse() && !studentCourse.isGrade10Course()) {

				if (studentCourse.getCourseCode().compareTo("CAPP") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("728"));
					} catch (AssertionError e) {
						errors.put("CAPP11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("AC") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("724"));
					} catch (AssertionError e) {
						errors.put("AC11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("KB") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("KB11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("CPCAN") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("732"));
					} catch (AssertionError e) {
						errors.put("CPCAN11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("CS") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("725"));
					} catch (AssertionError e) {
						errors.put("CS11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("SS") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("723"));
					} catch (AssertionError e) {
						errors.put("SS11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("DP") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("DP11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("MA") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("MA11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("EN") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("721"));
					} catch (AssertionError e) {
						errors.put("EN11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("FN") == 0 && studentCourse.getCourseLevel().compareTo("11") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("FN11", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("FA") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("FA12730", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("FA") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("720"));
					} catch (AssertionError e) {
						errors.put("FA12720", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("CAPP") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("729"));
					} catch (AssertionError e) {
						errors.put("CAPP12", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("AA") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("730"));
					} catch (AssertionError e) {
						errors.put("AA12730", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("AA") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("720"));
					} catch (AssertionError e) {
						errors.put("AA12720", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("EN") == 0 && studentCourse.getCourseLevel().compareTo("12") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("722"));
					} catch (AssertionError e) {
						errors.put("EN12", e);
					}

				}

				if (studentCourse.getCourseCode().compareTo("CPWE") == 0 && studentCourse.getCourseLevel().compareTo("12A") == 0) {
					try {
						assertTrue(studentCourse.getGradReqMet().contains("720"));
					} catch (AssertionError e) {
						errors.put("CPWE12A", e);
					}

				}
			}
		}
		errors.forEach((k,v)-> {
			logger.info("Course : "+k+", Error : "+v);
		});

		assertTrue(ruleProcessorData.isGraduated());
		assertTrue(errors.size()==0);
	}

	@Test
	public void testProcessGradAlgorithmRules_1996PF_104573159() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("1996-PF-104573159");
		assert ruleProcessorData != null;
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);

		assertTrue(!ruleProcessorData.isGraduated());
	}

	@Test
	public void testProcessGradAlgorithmRules_1996EN_109491597() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("1996-EN-109491597");
		assert ruleProcessorData != null;
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);

		assertTrue(!ruleProcessorData.isGraduated());
	}

	@Test
	public void testProcessGradAlgorithmRules_SCCP_130319387() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("SCCP-130319387");
		assert ruleProcessorData != null;
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);

		assertTrue(!ruleProcessorData.isGraduated());
	}

	@Test
	public void testProcessGradAlgorithmRules1950_122740988() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("1950-122740988");
		assert ruleProcessorData != null;
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);

		assertTrue(!ruleProcessorData.isGraduated());
	}
	//
	
	private RuleProcessorData getRuleProcessorData(String category) {
		File file = null;
		switch (category) {
			case "2018-EN":
				file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("2018-EN.json")).getFile());
				break;
			case "FI":
				file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("FI.json")).getFile());
				break;
			case "DD":
				file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("DD.json")).getFile());
				break;
			case "1950-EN":
				file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("1950-EN.json")).getFile());
				break;
			case "1950-EN-2":
				file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("1950-EN-2.json")).getFile());
				break;
			case "CP":
				file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("CP.json")).getFile());
				break;
			case "2018-PF":
				file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("2018-PF.json")).getFile());
				break;
			case "2018-EN-UNGRAD":
				file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("2018-EN-UNGRAD.json")).getFile());
				break;
			case "2018-EN-DUP-ASSMT":
				file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("2018-EN-DUP-ASSMT.json")).getFile());
				break;
			case "DD-FAIL":
				file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("DD-FAIL.json")).getFile());
				break;
			case "1950-EN-3":
				file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("1950-EN-3.json")).getFile());
				break;
			case "1950-EN-4":
				file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("1950-EN-4.json")).getFile());
				break;
			case "2018-EN-MIN-CREDIT-FAIL":
				file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("2018-EN-MIN-CREDIT-FAIL.json")).getFile());
				break;
			case "1996-EN-106945306":
				file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("1996-EN-106945306.json")).getFile());
				break;
			case "1996-EN-104337712":
				file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("1996-EN-104337712.json")).getFile());
				break;
			case "1996-EN-101821056":
				file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("1996-EN-101821056.json")).getFile());
				break;
			case "1996-EN-101541068":
				file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("1996-EN-101541068.json")).getFile());
				break;
			case "1996-EN-101171718":
				file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("1996-EN-101171718.json")).getFile());
				break;
			case "1996-EN-101039378":
				file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("1996-EN-101039378.json")).getFile());
				break;
			case "1996-PF-104573159":
				file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("1996-PF-104573159.json")).getFile());
				break;
			case "1996-EN-109491597":
				file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("1996-EN-109491597.json")).getFile());
				break;
			case "SCCP-130319387":
				file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("SCCP-130319387.json")).getFile());
				break;
			case "1950-122740988":
				file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("1950-122740988.json")).getFile());
				break;

		}
		RuleProcessorData data;
		try {
			data = new ObjectMapper().readValue(file, RuleProcessorData.class);
			return data;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return null;
	}
}
