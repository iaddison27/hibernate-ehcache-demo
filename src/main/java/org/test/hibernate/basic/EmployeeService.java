package org.test.hibernate.basic;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService implements IEmployeeService {

	@Autowired
	private IEmployeeDao employeeDao;
	
	@Override
	@Transactional
	public Employee saveOrUpdate(Employee employee) {
		return employeeDao.saveOrUpdate(employee);
	}
	
	@Override
	public Employee getById(long id) {
		return employeeDao.getById(id);
	}
	
	@Override
	public List<Employee> getAll() {
		return employeeDao.getAll();
	}
}
