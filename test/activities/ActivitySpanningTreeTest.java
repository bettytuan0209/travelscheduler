package activities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import schedulable.Activity;
import time.LegalTimeline;
import time.TimeBlock;

public class ActivitySpanningTreeTest {
	
	private ActivitySpanningTree ast;
	private static Activity activity1;
	private static Activity activity2;
	private static Activity activity3;
	private static ArrayList<TimeBlock> tbs;
	
	@BeforeClass
	public static void init() {
		activity1 = new Activity("activity1", new Duration(1), new Location(1,
				1), new LegalTimeline(new Interval(1, 30)));
		activity1.legalTimeline.schedule(1, 30);
		activity2 = new Activity("activity2", new Duration(2), new Location(1,
				1), new LegalTimeline(new Interval(1, 10)));
		activity2.legalTimeline.schedule(1, 10);
		activity3 = new Activity("activity3", new Duration(3), new Location(1,
				1), new LegalTimeline(new Interval(15, 20)));
		activity3.legalTimeline.schedule(15, 20);
		TimeBlock tb1 = new TimeBlock(0, new Interval(1, 10),
				new Location(0, 0), new Location(0, 0));
		TimeBlock tb2 = new TimeBlock(1, new Interval(15, 30), new Location(0,
				0), new Location(0, 0));
		tbs = new ArrayList<TimeBlock>();
		tbs.add(tb1);
		tbs.add(tb2);
	}
	
	@Test
	public void testActivitySpanningTree() {
		
		// An empty AST
		ast = new ActivitySpanningTree(0, tbs);
		Assert.assertEquals(0, ast.getIndex());
		Assert.assertEquals(tbs, ast.getAvailableTBs());
		Assert.assertTrue(ast.getActivities().isEmpty());
		Assert.assertEquals(0, ast.getSumActivitiesTime().getMillis());
	}
	
	@Test
	public void testAddActivities() {
		
		// Add a set of 2 activities
		Set<Activity> activities = new HashSet<Activity>();
		Assert.assertTrue(activities.add(activity1));
		Assert.assertTrue(activities.add(activity2));
		ast = new ActivitySpanningTree(0, tbs);
		Assert.assertTrue(ast.addActivities(activities));
		containsActivity1and2(ast);
		
		// Build AST of 1 activity
		ast = new ActivitySpanningTree(0, tbs);
		Assert.assertTrue(activities.remove(activity2));
		Assert.assertTrue(ast.addActivities(activities));
		
		containsActivity1(ast);
		
		// then add a set of 2 and 3, containing invalid, get only activity 1
		Assert.assertTrue(activities.remove(activity1));
		Assert.assertTrue(activities.add(activity2));
		Assert.assertTrue(activities.add(activity3));
		Assert.assertFalse(ast.addActivities(activities));
		containsActivity1(ast);
		
	}
	
	@Test
	public void testAddActivity() {
		ast = new ActivitySpanningTree(0, tbs);
		
		// Add activity 1
		Assert.assertTrue(ast.addActivity(activity1));
		containsActivity1(ast);
		
		// Add activity 2
		Assert.assertTrue(ast.addActivity(activity2));
		containsActivity1and2(ast);
		
		// Add activity 3
		Assert.assertFalse(ast.addActivity(activity3));
		containsActivity1and2(ast);
		
	}
	
	/* Private helper methods */
	
	private void containsActivity1(ActivitySpanningTree ast) {
		
		Assert.assertEquals(tbs, ast.getAvailableTBs());
		Assert.assertEquals(1, ast.getActivities().size());
		Iterator<Activity> itr = ast.getActivities().iterator();
		Assert.assertEquals(activity1, itr.next());
		Assert.assertEquals(1, ast.getSumActivitiesTime().getMillis());
		
	}
	
	private void containsActivity1and2(ActivitySpanningTree ast) {
		Assert.assertEquals(1, ast.getAvailableTBs().size());
		Assert.assertEquals(tbs.get(0), ast.getAvailableTBs().get(0));
		Assert.assertEquals(2, ast.getActivities().size());
		Iterator<Activity> itr = ast.getActivities().iterator();
		Assert.assertEquals(activity1, itr.next());
		Assert.assertEquals(activity2, itr.next());
		Assert.assertEquals(3, ast.getSumActivitiesTime().getMillis());
	}
}
