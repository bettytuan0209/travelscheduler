package activities;

import java.util.ArrayList;
import java.util.Iterator;

import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import schedulable.Activity;
import schedulable.Transportation;
import time.LegalTimeline;
import time.TimeBlock;

public class ActivitySpanningTreeTest {
	
	private ActivitySpanningTree ast;
	private static Activity activity1;
	private static Activity activity2;
	private static Activity activity3;
	private static Bridge bridge1and2;
	private static Bridge bridge2and3;
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
		bridge1and2 = new Bridge(new Transportation(new Duration(1)),
				activity1, activity2);
		bridge2and3 = new Bridge(new Transportation(new Duration(5)),
				activity2, activity3);
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
		
		// A standard AST
		ast = new ActivitySpanningTree(0, activity1);
		Assert.assertEquals(0, ast.getIndex());
		Assert.assertEquals(tbs, ast.getAvailableTBs(tbs));
		Assert.assertEquals(1, ast.getActivities().size());
		Assert.assertEquals(activity1, ast.getActivities().iterator().next());
		Assert.assertEquals(1, ast.getSumDuration().getMillis());
	}
	
	@Test
	public void testJoinAST() {
		
		// Build AST with activity 1
		ast = new ActivitySpanningTree(0, activity1);
		containsActivity1(ast);
		
		// Add activity 2
		ActivitySpanningTree otherAST = new ActivitySpanningTree(1, activity2);
		ast = ast.joinAST(otherAST, bridge1and2);
		containsActivity1and2(ast);
		
		// Add activity 3
		otherAST = new ActivitySpanningTree(2, activity3);
		ast = ast.joinAST(otherAST, bridge2and3);
		containsActivity1and2and3(ast);
		
		ast.clearBridges();
		Assert.assertTrue(ast.getBridges().isEmpty());
		Assert.assertEquals(6, ast.getSumDuration().getMillis());
	}
	
	/* Private helper methods */
	
	private void containsActivity1(ActivitySpanningTree ast) {
		
		Assert.assertEquals(tbs, ast.getAvailableTBs(tbs));
		Assert.assertEquals(1, ast.getActivities().size());
		Iterator<Activity> itr = ast.getActivities().iterator();
		Assert.assertEquals(activity1, itr.next());
		Assert.assertEquals(1, ast.getSumDuration().getMillis());
		Assert.assertEquals(1, ast.getSumActivitiesDuration().getMillis());
		Assert.assertTrue(ast.getBridges().isEmpty());
		
	}
	
	private void containsActivity1and2(ActivitySpanningTree ast) {
		Assert.assertEquals(1, ast.getAvailableTBs(tbs).size());
		Assert.assertEquals(tbs.get(0), ast.getAvailableTBs(tbs).get(0));
		Assert.assertEquals(2, ast.getActivities().size());
		Iterator<Activity> itr = ast.getActivities().iterator();
		Assert.assertEquals(activity1, itr.next());
		Assert.assertEquals(activity2, itr.next());
		Assert.assertEquals(4, ast.getSumDuration().getMillis());
		Assert.assertEquals(3, ast.getSumActivitiesDuration().getMillis());
		Assert.assertEquals(1, ast.getBridges().size());
		Assert.assertEquals(bridge1and2, ast.getBridges().iterator().next());
		
	}
	
	private void containsActivity1and2and3(ActivitySpanningTree ast) {
		Assert.assertEquals(0, ast.getAvailableTBs(tbs).size());
		Assert.assertEquals(3, ast.getActivities().size());
		Iterator<Activity> itr = ast.getActivities().iterator();
		Assert.assertEquals(activity1, itr.next());
		Assert.assertEquals(activity3, itr.next());
		Assert.assertEquals(activity2, itr.next());
		Assert.assertEquals(12, ast.getSumDuration().getMillis());
		Assert.assertEquals(6, ast.getSumActivitiesDuration().getMillis());
		Assert.assertEquals(2, ast.getBridges().size());
		Iterator<Bridge> itr2 = ast.getBridges().iterator();
		Assert.assertEquals(bridge1and2, itr2.next());
		Assert.assertEquals(bridge2and3, itr2.next());
	}
}
