package state;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

import org.jgrapht.graph.SimpleWeightedGraph;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.Test;

import schedulable.Activity;
import schedulable.LegalTime;
import schedulable.Schedulable;
import schedulable.Transportation;
import search.AStar;
import time.LegalTimeline;
import time.TimeBlock;
import util.DeepCopy;
import activities.Location;

public class SchedulingStateTest {
	SchedulingState state;
	
	@Test
	public void testSuccessors() {
		TimeBlock tb = new TimeBlock(1, new Interval(1, 30),
				new Location(0, 0), new Location(0, 0));
		Activity start = new Activity("At start location", new Duration(0),
				tb.getStartLocation());
		Activity end = new Activity("At end location", new Duration(0),
				tb.getEndLocation());
		
		ArrayList<TimeBlock> availableTBs = new ArrayList<TimeBlock>();
		availableTBs.add(tb);
		SimpleWeightedGraph<Location, Transportation> graph = new SimpleWeightedGraph<Location, Transportation>(
				Transportation.class);
		LegalTimeline legal1 = new LegalTimeline(new Interval(1, 30));
		Assert.assertTrue(legal1.schedule(new DateTime(1), new LegalTime(
				new Duration(20))));
		Activity museum = new Activity("museum", new Duration(2), new Location(
				1, 1), legal1);
		Activity concert = new Activity("concert", new Duration(3),
				new Location(2, 2), (LegalTimeline) DeepCopy.copy(legal1));
		Activity park = new Activity("park", new Duration(1),
				new Location(3, 3), (LegalTimeline) DeepCopy.copy(legal1));
		HashSet<Activity> activities = new HashSet<Activity>();
		activities.add(museum);
		activities.add(concert);
		activities.add(park);
		
		graph.addVertex(museum.location);
		graph.addVertex(concert.location);
		graph.addVertex(park.location);
		graph.addEdge(museum.location, concert.location, new Transportation(
				new Duration(3)));
		graph.addEdge(park.location, museum.location, new Transportation(
				new Duration(7)));
		graph.addEdge(park.location, concert.location, new Transportation(
				new Duration(2)));
		
		state = new SchedulingState(tb, graph, activities);
		
		ArrayList<SearchState> successors = new ArrayList<SearchState>();
		
		// For comparison
		SchedulingState first = null;
		SchedulingState second = null;
		SchedulingState third = null;
		
		SchedulingState parent = state;
		
		// depth 1:
		// 1. start - park
		// 2. start - museum
		// 3. start - concert
		successors = parent.successors();
		Assert.assertEquals(3, successors.size());
		for (int i = 0; i < successors.size(); i++) {
			SchedulingState child = (SchedulingState) successors.get(i);
			TreeMap<DateTime, Schedulable> map = child.getTb()
					.getScheduledActivities().getSchedule();
			switch ((int) child.getTb().lastEndTime().getMillis()) {
			case 5:
				// 0 - 0 start, 1 - 4 transportation, 4 - 5 park
				Assert.assertEquals(start, map.get(new DateTime(0)));
				Assert.assertEquals(new Transportation(new Duration(3)),
						map.get(new DateTime(1)));
				Assert.assertEquals(park, map.get(new DateTime(4)));
				Assert.assertFalse(child.checkGoal());
				first = child;
				break;
			
			case 7:
				// 0 - 0 start, 1 - 5 transportation, 5 - 7 museum
				Assert.assertEquals(start, map.get(new DateTime(0)));
				Assert.assertEquals(new Transportation(new Duration(4)),
						map.get(new DateTime(1)));
				Assert.assertEquals(museum, map.get(new DateTime(5)));
				parent = child;
				Assert.assertFalse(child.checkGoal());
				second = child;
				break;
			
			case 9:
				
				// 0 - 0 start, 1 - 6 transportation, 6 - 9 concert
				Assert.assertEquals(start, map.get(new DateTime(0)));
				Assert.assertEquals(new Transportation(new Duration(5)),
						map.get(new DateTime(1)));
				Assert.assertEquals(concert, map.get(new DateTime(6)));
				Assert.assertFalse(child.checkGoal());
				third = child;
				break;
			
			default:
				fail("cannot find successor match");
				break;
			}
			Assert.assertEquals(2, child.getActivities().size());
			
		}
		
		// test comparing
		Assert.assertTrue(first.compareTo(second) < 0);
		Assert.assertTrue(third.compareTo(second) > 0);
		AStar heap = new AStar();
		heap.add(third);
		heap.add(second);
		heap.add(first);
		Assert.assertEquals(first, (SchedulingState) heap.poll());
		Assert.assertEquals(second, (SchedulingState) heap.poll());
		Assert.assertEquals(third, (SchedulingState) heap.poll());
		Assert.assertEquals(null, (SchedulingState) heap.poll());
		
		// depth 2:
		// 1. start - museum - park
		// 2. start - museum - concert
		successors = parent.successors();
		Assert.assertEquals(2, successors.size());
		for (int i = 0; i < successors.size(); i++) {
			SchedulingState child = (SchedulingState) successors.get(i);
			TreeMap<DateTime, Schedulable> map = child.getTb()
					.getScheduledActivities().getSchedule();
			switch ((int) child.getTb().lastEndTime().getMillis()) {
			case 15:
				// 0 - 0 start, 1 - 5 transportation, 5 - 7 museum
				// 7 - 14 transportation, 14 - 15 park
				Assert.assertEquals(start, map.get(new DateTime(0)));
				Assert.assertEquals(new Transportation(new Duration(4)),
						map.get(new DateTime(1)));
				Assert.assertEquals(museum, map.get(new DateTime(5)));
				Assert.assertEquals(new Transportation(new Duration(7)),
						map.get(new DateTime(7)));
				Assert.assertEquals("park",
						((Activity) (map.get(new DateTime(14)))).title);
				parent = child;
				Assert.assertFalse(child.checkGoal());
				break;
			
			case 13:
				// 0 - 0 start, 1 - 5 transportation, 5 - 7 museum,
				// 7 - 10 transportation, 10 - 13 concert
				Assert.assertEquals(start, map.get(new DateTime(0)));
				Assert.assertEquals(new Transportation(new Duration(4)),
						map.get(new DateTime(1)));
				Assert.assertEquals(museum, map.get(new DateTime(5)));
				Assert.assertEquals(new Transportation(new Duration(3)),
						map.get(new DateTime(7)));
				Assert.assertEquals("concert",
						((Activity) (map.get(new DateTime(10)))).title);
				Assert.assertFalse(child.checkGoal());
				break;
			
			}
			Assert.assertEquals(1, child.getActivities().size());
		}
		
		// depth 3:
		// 1. start - museum - park - concert
		
		successors = parent.successors();
		Assert.assertEquals(1, successors.size());
		for (int i = 0; i < successors.size(); i++) {
			SchedulingState child = (SchedulingState) successors.get(i);
			TreeMap<DateTime, Schedulable> map = child.getTb()
					.getScheduledActivities().getSchedule();
			
			// 0 - 0 start, 1 - 5 transportation, 5 - 7 museum
			// 7 - 14 transportation, 14 - 15 park
			// 15 - 17 transportation, 17 - 20 concert
			Assert.assertEquals(start, map.get(new DateTime(0)));
			Assert.assertEquals(new Transportation(new Duration(4)),
					map.get(new DateTime(1)));
			Assert.assertEquals(museum, map.get(new DateTime(5)));
			Assert.assertEquals(new Transportation(new Duration(7)),
					map.get(new DateTime(7)));
			Assert.assertEquals("park",
					((Activity) (map.get(new DateTime(14)))).title);
			Assert.assertEquals(new Transportation(new Duration(2)),
					map.get(new DateTime(15)));
			Assert.assertEquals("concert",
					((Activity) (map.get(new DateTime(17)))).title);
			Assert.assertEquals(new DateTime(20), child.getTb().lastEndTime());
			parent = child;
			Assert.assertFalse(child.checkGoal());
		}
		
		// depth 4:
		// 1. start - museum - park - concert - end
		
		successors = parent.successors();
		Assert.assertEquals(1, successors.size());
		for (int i = 0; i < successors.size(); i++) {
			SchedulingState child = (SchedulingState) successors.get(i);
			TreeMap<DateTime, Schedulable> map = child.getTb()
					.getScheduledActivities().getSchedule();
			
			// 0 - 0 start, 1 - 5 transportation, 5 - 7 museum
			// 7 - 14 transportation, 14 - 15 park
			// 15 - 17 transportation, 17 - 20 concert
			// 20 - 25 transportation, 31 - 31 end
			Assert.assertEquals(start, map.get(new DateTime(0)));
			Assert.assertEquals(new Transportation(new Duration(4)),
					map.get(new DateTime(1)));
			Assert.assertEquals(museum, map.get(new DateTime(5)));
			Assert.assertEquals(new Transportation(new Duration(7)),
					map.get(new DateTime(7)));
			Assert.assertEquals("park",
					((Activity) (map.get(new DateTime(14)))).title);
			Assert.assertEquals(new Transportation(new Duration(2)),
					map.get(new DateTime(15)));
			Assert.assertEquals("concert",
					((Activity) (map.get(new DateTime(17)))).title);
			Assert.assertEquals(new Transportation(new Duration(5)),
					map.get(new DateTime(20)));
			Assert.assertEquals(end, map.get(new DateTime(31)));
			Assert.assertEquals(new DateTime(31), child.getTb().lastEndTime());
			Assert.assertTrue(child.checkGoal());
			
		}
		
		successors = successors.get(0).successors();
		Assert.assertTrue(successors.isEmpty());
	}
	
}
