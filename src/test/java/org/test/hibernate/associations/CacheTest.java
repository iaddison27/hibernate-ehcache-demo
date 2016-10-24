package org.test.hibernate.associations;

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
@ContextConfiguration(locations = { "classpath:org/test/hibernate/associations/repositoryTest-context.xml" })
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
		expected.add(createAndPersistEmployee("Ian", 120.0, Integer.valueOf(1), "NE1 1TT"));
		expected.add(createAndPersistEmployee("Dave", 200.0, Integer.valueOf(60), "NE20 3WW"));
		expected.add(createAndPersistEmployee("Robert", 99.0, Integer.valueOf(19), "NE24 5RR"));
		
		// Data persisted but no queries ran so all L2C stats should be 0
		printStats("After storing data", sessionFactory.getStatistics());
		assertEquals(0, sessionFactory.getStatistics().getSecondLevelCacheHitCount());
		assertEquals(0, sessionFactory.getStatistics().getSecondLevelCacheMissCount());
		assertEquals(0, sessionFactory.getStatistics().getSecondLevelCachePutCount());

		// First employee retrieved, won't be in cache so should see:
		// 0 cache hits
		// 1 cache miss
		// 2 cache puts (as it has also put the Address object in the cache)
		assertEquals(expected.get(0), employeeService.getById(expected.get(0).getId()));
		printStats("After get first employee", sessionFactory.getStatistics());
		assertEquals(0, sessionFactory.getStatistics().getSecondLevelCacheHitCount());
		assertEquals(1, sessionFactory.getStatistics().getSecondLevelCacheMissCount());
		assertEquals(2, sessionFactory.getStatistics().getSecondLevelCachePutCount());
		
		// Second employee retrieved, won't be in cache so should see:
		// 0 cache hits
		// 2 cache miss
		// 4 cache puts (as it has also put the Address object in the cache)
		assertEquals(expected.get(1), employeeService.getById(expected.get(1).getId()));
		printStats("After get second employee", sessionFactory.getStatistics());
		assertEquals(0, sessionFactory.getStatistics().getSecondLevelCacheHitCount());
		assertEquals(2, sessionFactory.getStatistics().getSecondLevelCacheMissCount());
		assertEquals(4, sessionFactory.getStatistics().getSecondLevelCachePutCount());
		
		// First employee retrieved (again), will be in cache so should see:
		// 2 cache hits (as it has also retrieved the Address object from the cache)
		// 2 cache miss
		// 4 cache puts (as it has also put the Address object in the cache)
		assertEquals(expected.get(0), employeeService.getById(expected.get(0).getId()));
		printStats("After get first employee again", sessionFactory.getStatistics());
		assertEquals(2, sessionFactory.getStatistics().getSecondLevelCacheHitCount());
		assertEquals(2, sessionFactory.getStatistics().getSecondLevelCacheMissCount());
		assertEquals(4, sessionFactory.getStatistics().getSecondLevelCachePutCount());
		
		// First employee retrieved (again), will be in cache so should see:
		// 4 cache hits (as it has also retrieved the Address object from the cache)
		// 2 cache miss
		// 4 cache puts
		assertEquals(expected.get(0), employeeService.getById(expected.get(0).getId()));
		printStats("After get first employee again (2)", sessionFactory.getStatistics());
		assertEquals(4, sessionFactory.getStatistics().getSecondLevelCacheHitCount());
		assertEquals(2, sessionFactory.getStatistics().getSecondLevelCacheMissCount());
		assertEquals(4, sessionFactory.getStatistics().getSecondLevelCachePutCount());
		
		// Third employee retrieved, won't be in cache so should see:
		// 4 cache hits
		// 3 cache miss
		// 6 cache puts (as it has also put the Address object in the cache)
		assertEquals(expected.get(2), employeeService.getById(expected.get(2).getId()));
		printStats("After get third employee", sessionFactory.getStatistics());
		assertEquals(4, sessionFactory.getStatistics().getSecondLevelCacheHitCount());
		assertEquals(3, sessionFactory.getStatistics().getSecondLevelCacheMissCount());
		assertEquals(6, sessionFactory.getStatistics().getSecondLevelCachePutCount());
	}
	
	private Employee createAndPersistEmployee(String name, double salary, Integer houseNumber, String postcode) {
		Address a = new Address(houseNumber, postcode, null);
		Employee e = new Employee(name, salary, a);
		a.setEmployee(e);
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
