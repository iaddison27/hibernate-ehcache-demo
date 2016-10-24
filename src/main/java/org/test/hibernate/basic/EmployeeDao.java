package org.test.hibernate.basic;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;

@Component
public class EmployeeDao implements IEmployeeDao {

	@PersistenceContext
    private EntityManager manager;
	
	@Override
	public Employee saveOrUpdate(Employee employee) {
		return manager.merge(employee);
	}

	@Override
	public Employee getById(long id) {
		return manager.find(Employee.class, id);
	}

	@Override
	public List<Employee> getAll() {
		Query query = manager.createNamedQuery("Employee.findAll");            
        return (List<Employee>) query.getResultList();
	}

}
