package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CompensationRepository compensationRepository;

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Creating employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

    @Override
    public ReportingStructure employeeReports(String id) {
        LOG.debug("Creating ReportingStructure for Employee with employeeId [{}]", id);

        // If the employeeId is invalid, the RuntimeException in read() will be thrown.  This is reasonable here too.
        Employee employee = read(id);

        ReportingStructure reportingStructure = new ReportingStructure();
        reportingStructure.setEmployee(employee);
        reportingStructure.setNumberOfReports(countEmployeeReports(employee));

        return reportingStructure;
    }

    public Compensation createCompensation(Compensation compensation) {
        LOG.debug("Creating compensation [{}]", compensation);

        compensationRepository.insert(compensation);

        return compensation;
    }

    public Compensation readCompensation(String id) {
        LOG.debug("Querying compensation with employee id [{}]", id);

        Compensation compensation = compensationRepository.findByEmployeeEmployeeId(id);

        if (compensation == null) {
            throw new RuntimeException("Invalid employeeId for compensation: " + id);
        }

        return compensation;
    }

    // Helper function to count reports for an employee
    private long countEmployeeReports(Employee employee) {
        if (employee == null || employee.getDirectReports() == null || employee.getDirectReports().size() == 0) {
            return 0L;
        }

        Set<String> countedEmployeeIds = new HashSet<>();
        long count = 0L;
        Queue<Employee> breadthFirstTraversal = new LinkedList<>(employee.getDirectReports());
        while (breadthFirstTraversal.size() > 0) {
            Employee currentEmployee = breadthFirstTraversal.remove();
            if (currentEmployee != null) {
                // Sample data shows that Employee object(s) in Direct Report list may be incomplete.
                // For an example, all fields are NULL except for employeeId on John Lennon's direct reports.
                // Call function read() using the employeeId to get a 'complete' Employee object
                if (currentEmployee.getEmployeeId() == null || currentEmployee.getEmployeeId().length() == 0 ||
                        countedEmployeeIds.contains(currentEmployee.getEmployeeId())) {
                    continue;
                }
                try{
                    currentEmployee = read(currentEmployee.getEmployeeId());

                    // increment for currentEmployee
                    count++;

                    // add current employee to counted HashSet
                    countedEmployeeIds.add(currentEmployee.getEmployeeId());

                    // Check currentEmployee for their direct reports.  If any direct reports exist, add to queue.
                    if (currentEmployee.getDirectReports() != null && currentEmployee.getDirectReports().size() > 0) {
                        breadthFirstTraversal.addAll(currentEmployee.getDirectReports());
                    }
                }
                catch (RuntimeException e) {
                    // read() method returned an exception.
                    // Don't increment count & continue to the next currentEmployee in queue
                }
            }
        }

        return count;
    }
}
