package dzcorp.jpa.service;

import dzcorp.jpa.model.Employee;
import dzcorp.jpa.model.EmployeePage;
import dzcorp.jpa.model.EmployeeSearchCriteria;
import dzcorp.jpa.persistence.EmployeeGateway;
import dzcorp.jpa.persistence.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeGateway employeeCriteriaRepository;

    public EmployeeService(EmployeeRepository employeeRepository, EmployeeGateway employeeCriteriaRepository) {
        this.employeeRepository = employeeRepository;
        this.employeeCriteriaRepository = employeeCriteriaRepository;
    }

    public Page<Employee> getEmployees(EmployeePage employeePage,
                                       EmployeeSearchCriteria employeeSearchCriteria) {
        return employeeCriteriaRepository.findAllWithFilters(employeePage, employeeSearchCriteria);
    }

    public List<Employee> filterByName(EmployeeSearchCriteria employeeSearchCriteria) {
        return employeeCriteriaRepository.getByFilter(employeeSearchCriteria);
    }

    public Employee addEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }
}
