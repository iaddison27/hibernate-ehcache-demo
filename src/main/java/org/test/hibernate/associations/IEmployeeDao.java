package org.test.hibernate.associations;

import java.util.List;

public interface IEmployeeDao {

	public Employee saveOrUpdate(Employee employee);
	
	public Employee getById(long id);
	
	public List<Employee> getAll();
}
