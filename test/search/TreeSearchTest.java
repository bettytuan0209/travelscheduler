package search;

import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import schedulable.Activity;
import schedulable.Schedulable;
import state.SchedulingState;
import state.SchedulingStateTest;
import activities.Location;

public class TreeSearchTest {
	TreeSearch search;
	static SchedulingState initial;
	
	@BeforeClass
	public static void init() {
		SchedulingStateTest.initHelper();
		initial = SchedulingStateTest.state1;
	}
	
	@Test
	public void testTreeSearch() {
		search = new TreeSearch(new AStar(), initial);
		Assert.assertNotNull(search);
		Assert.assertTrue(search instanceof TreeSearch);
		Assert.assertEquals(0, search.getNumExpanded());
	}
	
	@Test
	public void testNextGoal() {
		search = new TreeSearch(new AStar(), initial);
		SchedulingState goal = (SchedulingState) search.nextGoal();
		Assert.assertTrue(goal.activities.isEmpty());
		
		TreeMap<DateTime, Schedulable> map = goal.getTb()
				.getScheduledActivities().getSchedule();
		
		// 0 - 0 start, 1 - 5 transportation, 5 - 7 museum
		// 7 - 10 transportation, 10 - 13 concert
		// 13 - 15 transportation, 15 - 16 park
		// 16 - 19 transportation, 19 - 19 end
		Assert.assertEquals(new Location(0, 0),
				((Activity) (map.get(new DateTime(0)))).location);
		Assert.assertEquals(new Duration(4), map.get(new DateTime(1))
				.getDuration());
		Assert.assertEquals("museum",
				((Activity) (map.get(new DateTime(5)))).title);
		Assert.assertEquals(new Duration(3), map.get(new DateTime(7))
				.getDuration());
		Assert.assertEquals("concert",
				((Activity) (map.get(new DateTime(10)))).title);
		Assert.assertEquals(new Duration(2), map.get(new DateTime(13))
				.getDuration());
		Assert.assertEquals("park",
				((Activity) (map.get(new DateTime(15)))).title);
		Assert.assertEquals(new Duration(3), map.get(new DateTime(16))
				.getDuration());
		Assert.assertEquals(new Location(0, 0),
				((Activity) (map.get(new DateTime(19)))).location);
		Assert.assertEquals(new DateTime(19), goal.getTb().lastEndTime());
		Assert.assertTrue(goal.checkGoal());
		
	}
	
}
