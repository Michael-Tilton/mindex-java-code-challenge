package com.mindex.challenge.data;

public class ReportingStructure {
    private Employee employee;
    private Long numberOfReports;

    public ReportingStructure() {
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Long getNumberOfReports() {
        return numberOfReports;
    }

    public void setNumberOfReports(Long numberOfReports) {
        this.numberOfReports = numberOfReports;
    }

}
