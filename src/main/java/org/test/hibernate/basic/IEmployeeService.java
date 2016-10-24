package org.test.hibernate.basic;

import java.util.List;

public interface IEmployeeService {

	public Employee saveOrUpdate(Employee employee);
	
	public Employee getById(long id);
	
	public List<Employee> getAll();
}
