package org.test.hibernate.basic;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:org/test/hibernate/basic/repositoryTest-context.xml" })
public class CacheTest {

	@Autowired
	protected IEmployeeService employeeService;
	
	@Autowired
	protected SessionFactory sessionFactory;
	
	@Before
	public void setup() {
		// Reset Hibernate statistics between tests
		// Note: statistics are not reset during a test, so are culmative during a test
		sessionFactory.getStatistics().clear();
	}
	
	@Test
	public final void cacheByIdShouldWorkCorrectly() throws Exception {
		// Test data
		List<Employee> expected = new ArrayList<>();
		expected.add(createAndPersistEmployee("Ian", 120.0));
		expected.add(createAndPersistEmployee("Dave", 200.0));
		expected.add(createAndPersistEmployee("Robert", 99.0));
		
		// Data persisted but no queries ran so all L2C stats should be 0
		printStats("After storing data", sessionFactory.getStatistics());
		assertEquals(0, sessionFactory.getStatistics().getSecondLevelCacheHitCount());
		assertEquals(0, sessionFactory.getStatistics().getSecondLevelCacheMissCount());
		assertEquals(0, sessionFactory.getStatistics().getSecondLevelCachePutCount());

		// First employee retrieved, won't be in cache so should see:
		// 0 cache hits
		// 1 cache miss
		// 1 cache puts
		assertEquals(expected.get(0), employeeService.getById(expected.get(0).getId()));
		printStats("After get first employee", sessionFactory.getStatistics());
		assertEquals(0, sessionFactory.getStatistics().getSecondLevelCacheHitCount());
		assertEquals(1, sessionFactory.getStatistics().getSecondLevelCacheMissCount());
		assertEquals(1, sessionFactory.getStatistics().getSecondLevelCachePutCount());
		
		// Second employee retrieved, won't be in cache so should see:
		// 0 cache hits
		// 2 cache miss
		// 2 cache puts
		assertEquals(expected.get(1), employeeService.getById(expected.get(1).getId()));
		printStats("After get second employee", sessionFactory.getStatistics());
		assertEquals(0, sessionFactory.getStatistics().getSecondLevelCacheHitCount());
		assertEquals(2, sessionFactory.getStatistics().getSecondLevelCacheMissCount());
		assertEquals(2, sessionFactory.getStatistics().getSecondLevelCachePutCount());
		
		// First employee retrieved (again), will be in cache so should see:
		// 1 cache hits
		// 2 cache miss
		// 2 cache puts
		assertEquals(expected.get(0), employeeService.getById(expected.get(0).getId()));
		printStats("After get first employee again", sessionFactory.getStatistics());
		assertEquals(1, sessionFactory.getStatistics().getSecondLevelCacheHitCount());
		assertEquals(2, sessionFactory.getStatistics().getSecondLevelCacheMissCount());
		assertEquals(2, sessionFactory.getStatistics().getSecondLevelCachePutCount());
		
		// First employee retrieved (again), will be in cache so should see:
		// 2 cache hits
		// 2 cache miss
		// 2 cache puts
		assertEquals(expected.get(0), employeeService.getById(expected.get(0).getId()));
		printStats("After get first employee again (2)", sessionFactory.getStatistics());
		assertEquals(2, sessionFactory.getStatistics().getSecondLevelCacheHitCount());
		assertEquals(2, sessionFactory.getStatistics().getSecondLevelCacheMissCount());
		assertEquals(2, sessionFactory.getStatistics().getSecondLevelCachePutCount());
		
		// Third employee retrieved, won't be in cache so should see:
		// 2 cache hits
		// 3 cache miss
		// 3 cache puts
		assertEquals(expected.get(2), employeeService.getById(expected.get(2).getId()));
		printStats("After get third employee", sessionFactory.getStatistics());
		assertEquals(2, sessionFactory.getStatistics().getSecondLevelCacheHitCount());
		assertEquals(3, sessionFactory.getStatistics().getSecondLevelCacheMissCount());
		assertEquals(3, sessionFactory.getStatistics().getSecondLevelCachePutCount());
	}
	
	@Test
	public final void queryCacheShouldWorkCorrectly() throws Exception {
		// Test data
		List<Employee> expected = new ArrayList<>();
		expected.add(createAndPersistEmployee("Ian", 120.0));
		expected.add(createAndPersistEmployee("Dave", 200.0));
		expected.add(createAndPersistEmployee("Robert", 99.0));
		
		// Data persisted but no queries ran so all L2C stats should be 0
		printStats("After storing data", sessionFactory.getStatistics());
		assertEquals(0, sessionFactory.getStatistics().getSecondLevelCacheHitCount());
		assertEquals(0, sessionFactory.getStatistics().getSecondLevelCacheMissCount());
		assertEquals(0, sessionFactory.getStatistics().getSecondLevelCachePutCount());
		
		// Query to retrieve all employees, won't be in cache so should see:
		// 0 cache hits
		// 0 cache miss
		// 3 cache puts (there are 3 employees in total)
		assertEquals(expected, employeeService.getAll());
		printStats("After retrieving all employees", sessionFactory.getStatistics());
		assertEquals(0, sessionFactory.getStatistics().getSecondLevelCacheHitCount());
		assertEquals(0, sessionFactory.getStatistics().getSecondLevelCacheMissCount());
		assertEquals(3, sessionFactory.getStatistics().getSecondLevelCachePutCount());
		
		// Query to retrieve all employees, will be in cache so should see:
		// 4 cache hits
		// 0 cache miss
		// 3 cache puts
		assertEquals(expected, employeeService.getAll());
		printStats("After retrieving all employees again", sessionFactory.getStatistics());
		assertEquals(3, sessionFactory.getStatistics().getSecondLevelCacheHitCount());
		assertEquals(0, sessionFactory.getStatistics().getSecondLevelCacheMissCount());
		assertEquals(3, sessionFactory.getStatistics().getSecondLevelCachePutCount());
	}
	
	@Test
	public final void cacheShouldWorkCorrectlyWhenEntitiesAreUpdated() throws Exception {
		// Test data
		List<Employee> expected = new ArrayList<>();
		expected.add(createAndPersistEmployee("Ian", 120.0));
		expected.add(createAndPersistEmployee("Dave", 200.0));
		expected.add(createAndPersistEmployee("Robert", 99.0));
		
		// Data persisted but no queries ran so all L2C stats should be 0
		printStats("After storing data", sessionFactory.getStatistics());
		assertEquals(0, sessionFactory.getStatistics().getSecondLevelCacheHitCount());
		assertEquals(0, sessionFactory.getStatistics().getSecondLevelCacheMissCount());
		assertEquals(0, sessionFactory.getStatistics().getSecondLevelCachePutCount());
		
		// First employee retrieved, won't be in cache so should see:
		// 0 cache hits
		// 1 cache miss
		// 1 cache puts
		assertEquals(expected.get(0), employeeService.getById(expected.get(0).getId()));
		printStats("After get first employee", sessionFactory.getStatistics());
		assertEquals(0, sessionFactory.getStatistics().getSecondLevelCacheHitCount());
		assertEquals(1, sessionFactory.getStatistics().getSecondLevelCacheMissCount());
		assertEquals(1, sessionFactory.getStatistics().getSecondLevelCachePutCount());
		
		// First employee retrieved (again), will be in cache so should see:
		// 1 cache hits
		// 1 cache miss
		// 1 cache puts
		assertEquals(expected.get(0), employeeService.getById(expected.get(0).getId()));
		printStats("After get first employee again", sessionFactory.getStatistics());
		assertEquals(1, sessionFactory.getStatistics().getSecondLevelCacheHitCount());
		assertEquals(1, sessionFactory.getStatistics().getSecondLevelCacheMissCount());
		assertEquals(1, sessionFactory.getStatistics().getSecondLevelCachePutCount());
		
		// Modify the first employee
		Employee employee = expected.get(0);
		employee.setName("Bill");
		employeeService.saveOrUpdate(employee);
		
		// (Modified) first employee retrieved, as it's been amended it won't be in cache so should see:
		// 3 cache hits (one of these will have been from the saveOrUpdate call)
		// 1 cache miss
		// 2 cache puts
		Employee actual = employeeService.getById(expected.get(0).getId());
		assertEquals(employee, actual);
		assertEquals("Bill", actual.getName());
		printStats("After get first employee (after modification)", sessionFactory.getStatistics());
		assertEquals(3, sessionFactory.getStatistics().getSecondLevelCacheHitCount());
		assertEquals(1, sessionFactory.getStatistics().getSecondLevelCacheMissCount());
		assertEquals(2, sessionFactory.getStatistics().getSecondLevelCachePutCount());
	}
	
	private Employee createAndPersistEmployee(String name, double salary) {
		Employee e = new Employee(name, salary);
		return employeeService.saveOrUpdate(e);
	}

	private void printStats(String title, Statistics stats) {
		System.out.println(title);
		System.out.println("Fetch Count=" + stats.getEntityFetchCount());
		System.out.println("Second Level Hit Count=" + stats.getSecondLevelCacheHitCount());
		System.out.println("Second Level Miss Count=" + stats.getSecondLevelCacheMissCount());
		System.out.println("Second Level Put Count=" + stats.getSecondLevelCachePutCount());
		
		System.out.println("Second Level =" + stats.getSecondLevelCacheStatistics("employee"));
	}
}
