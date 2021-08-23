package com.mindex.challenge;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.impl.EmployeeServiceImplTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ChallengeApplicationTests {

	private String employeeCreateUrl;
	private String employeeReportsQueryUrl;
	private String employeeCompensationCreateUrl;
	private String employeeCompensationQueryUrl;

	@Autowired
	private TestRestTemplate restTemplate;

	@LocalServerPort
	private int port;

	@Before
	public void setup() {
		employeeCreateUrl = "http://localhost:" + port + "/employee";
		employeeReportsQueryUrl = "http://localhost:" + port + "/employeeReports/{id}";
		employeeCompensationCreateUrl = "http://localhost:" + port + "/employeeCompensation";
		employeeCompensationQueryUrl = "http://localhost:" + port + "/employeeCompensation/{id}";
	}

	@Test
	public void contextLoads() {
		assertNotNull(restTemplate);
	}

	@Test
	public void testEmployeeReports() {
		// Employee
		Employee testEmployee = createTestEmployee("It's-A Me", "Mario", "Plumber");
		Employee createdEmployee = restTemplate.postForEntity(
				employeeCreateUrl, testEmployee, Employee.class).getBody();
		assertNotNull(createdEmployee);
		String createdEmployeeId = createdEmployee.getEmployeeId();
		EmployeeServiceImplTest.assertEmployeeEquivalence(testEmployee, createdEmployee);

		// Employee has 0 direct reports
		ReportingStructure createdReportingStructureEmployee = restTemplate.getForEntity(
				employeeReportsQueryUrl, ReportingStructure.class, createdEmployeeId).getBody();
		assertNotNull(createdReportingStructureEmployee);
		assertNotNull(createdReportingStructureEmployee.getNumberOfReports());
		assertEquals(createdReportingStructureEmployee.getNumberOfReports().longValue(), 0L);

		// Manager
		Employee testManager = createTestManager(createdEmployeeId);
		Employee createdManager = restTemplate.postForEntity(
				employeeCreateUrl, testManager, Employee.class).getBody();
		assertNotNull(createdManager);
		String createdManagerId = createdManager.getEmployeeId();
		EmployeeServiceImplTest.assertEmployeeEquivalence(testManager, createdManager);

		// Manager has 1 direct report
		ReportingStructure createdReportingStructureManager = restTemplate.getForEntity(
				employeeReportsQueryUrl, ReportingStructure.class, createdManagerId).getBody();
		assertNotNull(createdReportingStructureManager);
		assertNotNull(createdReportingStructureManager.getNumberOfReports());
		assertEquals(createdReportingStructureManager.getNumberOfReports().longValue(), 1L);
	}

	@Test
	public void testEmployeeCompensation() {
		// Employee
		Employee newHire = createTestEmployee("Somebody", "Else", "Developer");
		String newHireId = UUID.randomUUID().toString();
		newHire.setEmployeeId(newHireId);

		// Compensation
		Compensation testCompensation = new Compensation();
		testCompensation.setEmployee(newHire);
		testCompensation.setSalary(100000L);
		testCompensation.setEffectiveDate(new Date());

		Compensation createdCompensation = restTemplate.postForEntity(
				employeeCompensationCreateUrl, testCompensation, Compensation.class).getBody();
		assertNotNull(createdCompensation);
		assertCompensationEquivalence(testCompensation, createdCompensation);

		Compensation queryCompensation = restTemplate.getForEntity(
				employeeCompensationQueryUrl, Compensation.class, newHireId).getBody();

		assertNotNull(queryCompensation);
		assertCompensationEquivalence(createdCompensation, queryCompensation);
	}

	private static Employee createTestEmployee(String firstName, String lastName, String position) {
		Employee testEmployee = new Employee();
		testEmployee.setFirstName(firstName);
		testEmployee.setLastName(lastName);
		testEmployee.setDepartment("Engineering");
		testEmployee.setPosition(position);

		return testEmployee;
	}

	private static Employee createTestManager(String directReportEmployeeId) {
		Employee testManager = createTestEmployee("Princess", "Peach", "Manager");

		Employee testDirectReport = new Employee();
		testDirectReport.setEmployeeId(directReportEmployeeId);
		List<Employee> directReports = new ArrayList<>();
		directReports.add(testDirectReport);
		testManager.setDirectReports(directReports);

		return testManager;
	}

	private static void assertCompensationEquivalence(Compensation expected, Compensation actual) {
		EmployeeServiceImplTest.assertEmployeeEquivalence(expected.getEmployee(), actual.getEmployee());
		assertEquals(expected.getSalary(), actual.getSalary());
		assertEquals(expected.getEffectiveDate(), actual.getEffectiveDate());
	}
}
