package ca.bc.gov.educ.api.ruleengine.service;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class RuleEngineServiceTest {

	@Autowired
	private RuleEngineService ruleEngineService;
	
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
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_2018EN_FI_projectedfalse() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("FI");	
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_2018PF_DD_projectedfalse() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("DD");
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_1950EN_projectedfalse() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("1950-EN");
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_1950EN_2_projectedfalse() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("1950-EN-2");
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_1950EN_3_projectedfalse() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("1950-EN-3");	
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_1950EN_4_projectedfalse() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("1950-EN-4");
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_2018EN_CP_projectedfalse() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("CP");	
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_2018PF_projectedfalse() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("2018-PF");	
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_2018EN_projectedfalse() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("2018-EN-UNGRAD");	
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_2018EN_Dup_Asmt_projectedfalse() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("2018-EN-DUP-ASSMT");
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_2018PF_DD_Fail_projectedfalse() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("DD-FAIL");	
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	
	@Test
	public void testProcessGradAlgorithmRules_2018EN_MINCREDITS_Fail_projectedfalse() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("2018-EN-MIN-CREDIT-FAIL");	
		ruleProcessorData.setProjected(false);
		ruleProcessorData = ruleEngineService.processGradAlgorithmRules(ruleProcessorData);
		assertNotNull(ruleProcessorData);
	}
	//
	
	private RuleProcessorData getRuleProcessorData(String category) {
		File file = null;
		if(category.equals("2018-EN")) {
			file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("2018-EN.json")).getFile());
		}else if(category.equals("FI")) {
			file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("FI.json")).getFile());
		}else if(category.equals("DD")) {
			file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("DD.json")).getFile());
		}else if(category.equals("1950-EN")) {
			file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("1950-EN.json")).getFile());
		}else if(category.equals("1950-EN-2")) {
			file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("1950-EN-2.json")).getFile());
		}else if(category.equals("CP")) {
			file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("CP.json")).getFile());
		}else if(category.equals("2018-PF")) {
			file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("2018-PF.json")).getFile());
		}else if(category.equals("2018-EN-UNGRAD")) {
			file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("2018-EN-UNGRAD.json")).getFile());
		}else if(category.equals("2018-EN-DUP-ASSMT")) {
			file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("2018-EN-DUP-ASSMT.json")).getFile());
		}else if(category.equals("DD-FAIL")) {
			file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("DD-FAIL.json")).getFile());
		}else if(category.equals("1950-EN-3")) {
			file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("1950-EN-3.json")).getFile());
		}else if(category.equals("1950-EN-4")) {
			file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("1950-EN-4.json")).getFile());
		}else if(category.equals("2018-EN-MIN-CREDIT-FAIL")) {
			file = new File(Objects.requireNonNull(RuleEngineServiceTest.class.getClassLoader().getResource("2018-EN-MIN-CREDIT-FAIL.json")).getFile());
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
