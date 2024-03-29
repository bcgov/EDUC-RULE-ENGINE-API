package ca.bc.gov.educ.api.ruleengine.controller;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.educ.api.ruleengine.dto.RuleProcessorData;
import ca.bc.gov.educ.api.ruleengine.service.RuleEngineService;
import ca.bc.gov.educ.api.ruleengine.service.RuleEngineServiceTest;


@ExtendWith(MockitoExtension.class)
public class RuleEngineControllerTest {

	@Mock
	private RuleEngineService ruleEngineService;
	
	@InjectMocks
	private RuleEngineController ruleEngineController;
	
	@Test
	public void testProcessGradAlgorithmRules() {
		RuleProcessorData ruleProcessorData = getRuleProcessorData("2018-EN");
		
		Mockito.when(ruleEngineService.processGradAlgorithmRules(ruleProcessorData)).thenReturn(ruleProcessorData);
		ruleEngineController.processGradAlgorithmRules(ruleProcessorData);
		Mockito.verify(ruleEngineService).processGradAlgorithmRules(ruleProcessorData);
	}
	
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
