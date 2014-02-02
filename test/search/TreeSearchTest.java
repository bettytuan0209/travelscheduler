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
import time.TimeBlock;
import activities.Location;

public class TreeSearchTest {
	TreeSearch search;
	static SchedulingState state1;
	static SchedulingState state2;
	
	@BeforeClass
	public static void init() {
		SchedulingStateTest.initHelper();
		state1 = SchedulingStateTest.state1;
		state2 = SchedulingStateTest.state2;
		
	}
	
	@Test
	public void testTreeSearch() {
		search = new TreeSearch(new AStar(), state1);
		Assert.assertNotNull(search);
		Assert.assertTrue(search instanceof TreeSearch);
		Assert.assertEquals(0, search.getNumExpanded());
	}
	
	@Test
	public void testNextGoal1() {
		search = new TreeSearch(new AStar(), state1);
		SchedulingState goal = (SchedulingState) search.nextGoal();
		Assert.assertTrue(goal.activities.isEmpty());
		testTb1Goal(goal.getTb());
		
		Assert.assertTrue(goal.checkGoal());
	}
	
	@Test
	public void testNextGoal2() {
		search = new TreeSearch(new AStar(), state2);
		SchedulingState goal = (SchedulingState) search.nextGoal();
		Assert.assertTrue(goal.activities.isEmpty());
		testTb2Goal(goal.getTb());
		
		Assert.assertTrue(goal.checkGoal());
	}
	
	// Allow other tests to call
	public static void testTb1Goal(TimeBlock tb) {
		TreeMap<DateTime, Schedulable> map = tb.getTimeline()
				.getSchedule();
		
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
		Assert.assertEquals(new DateTime(19), tb.lastEndTime());
	}
	
	// Allow other tests to call
	public static void testTb2Goal(TimeBlock tb) {
		TreeMap<DateTime, Schedulable> map = tb.getTimeline()
				.getSchedule();
		
		// 7 - 7 start, 8 - 9 transportation, 9 - 12 temple
		// 12 - 17 transportation, 17 - 21 gallery
		// 21 - 24 transportation, 24 - 26 beach
		// 26 - 29 transportation, 29 - 29 end
		Assert.assertEquals(new Location(0, 0),
				((Activity) (map.get(new DateTime(7)))).location);
		Assert.assertEquals(new Duration(1), map.get(new DateTime(8))
				.getDuration());
		Assert.assertEquals("temple",
				((Activity) (map.get(new DateTime(9)))).title);
		Assert.assertEquals(new Duration(5), map.get(new DateTime(12))
				.getDuration());
		Assert.assertEquals("gallery",
				((Activity) (map.get(new DateTime(17)))).title);
		Assert.assertEquals(new Duration(3), map.get(new DateTime(21))
				.getDuration());
		Assert.assertEquals("beach",
				((Activity) (map.get(new DateTime(24)))).title);
		Assert.assertEquals(new Duration(3), map.get(new DateTime(26))
				.getDuration());
		Assert.assertEquals(new Location(0, 0),
				((Activity) (map.get(new DateTime(29)))).location);
		Assert.assertEquals(new DateTime(29), tb.lastEndTime());
		
	}
	
	public static void testTb3Goal(TimeBlock tb) {
		TreeMap<DateTime, Schedulable> map = tb.getTimeline()
				.getSchedule();
		
		// 0 - 0 start, 1 - 6 transportation, 6 - 10 hiking
		// 10 - 13 transportation, 15 - 20 skiing
		// 20 - 22 transportation, 22 - 22 end
		Assert.assertEquals(new Location(0, 0),
				((Activity) (map.get(new DateTime(0)))).location);
		Assert.assertEquals(new Duration(5), map.get(new DateTime(1))
				.getDuration());
		Assert.assertEquals("hiking",
				((Activity) (map.get(new DateTime(6)))).title);
		Assert.assertEquals(new Duration(3), map.get(new DateTime(10))
				.getDuration());
		Assert.assertEquals("skiing",
				((Activity) (map.get(new DateTime(15)))).title);
		Assert.assertEquals(new Duration(2), map.get(new DateTime(20))
				.getDuration());
		Assert.assertEquals(new Location(0, 0),
				((Activity) (map.get(new DateTime(22)))).location);
		Assert.assertEquals(new DateTime(22), tb.lastEndTime());
		
	}
	
}
