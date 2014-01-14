package state;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import org.jgrapht.graph.SimpleWeightedGraph;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import schedulable.Activity;
import schedulable.LegalTime;
import schedulable.Schedulable;
import schedulable.Transportation;
import search.AStar;
import time.LegalTimeline;
import time.TimeBlock;
import util.Debugger;
import util.DeepCopy;
import activities.Location;

public class SchedulingStateTest {
	public static SchedulingState state1; // Basic. Every activity has the same
											// legal time. Only check for
											// optimization
	public static SchedulingState state2; // Forwarchecking. Recognize end ends
	public static SchedulingState state3; // Waits between schedule due to late
											// legaltimes
	public static Activity start, end;
	public static SimpleWeightedGraph<Location, Transportation> graph = new SimpleWeightedGraph<Location, Transportation>(
			Transportation.class);
	public static TimeBlock tb1;
	public static TimeBlock tb2;
	public static TimeBlock tb3;
	
	@BeforeClass
	public static void init() {
		initHelper();
	}
	
	@Test
	public void testSuccessors1() {
		
		ArrayList<SearchState> successors = new ArrayList<SearchState>();
		
		// For comparison
		SchedulingState first = null;
		SchedulingState second = null;
		SchedulingState third = null;
		
		SchedulingState parent = state1;
		
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
				Assert.assertEquals(new Duration(3), map.get(new DateTime(1))
						.getDuration());
				Assert.assertEquals("park",
						((Activity) (map.get(new DateTime(4)))).title);
				Assert.assertFalse(child.checkGoal());
				first = child;
				break;
			
			case 7:
				// 0 - 0 start, 1 - 5 transportation, 5 - 7 museum
				Assert.assertEquals(start, map.get(new DateTime(0)));
				Assert.assertEquals(new Duration(4), map.get(new DateTime(1))
						.getDuration());
				Assert.assertEquals("museum",
						((Activity) (map.get(new DateTime(5)))).title);
				parent = child;
				Assert.assertFalse(child.checkGoal());
				second = child;
				break;
			
			case 9:
				
				// 0 - 0 start, 1 - 6 transportation, 6 - 9 concert
				Assert.assertEquals(start, map.get(new DateTime(0)));
				Assert.assertEquals(new Duration(5), map.get(new DateTime(1))
						.getDuration());
				Assert.assertEquals("concert",
						((Activity) (map.get(new DateTime(6)))).title);
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
				Assert.assertEquals(new Duration(4), map.get(new DateTime(1))
						.getDuration());
				Assert.assertEquals("museum",
						((Activity) (map.get(new DateTime(5)))).title);
				Assert.assertEquals(new Duration(7), map.get(new DateTime(7))
						.getDuration());
				Assert.assertEquals("park",
						((Activity) (map.get(new DateTime(14)))).title);
				parent = child;
				Assert.assertFalse(child.checkGoal());
				break;
			
			case 13:
				// 0 - 0 start, 1 - 5 transportation, 5 - 7 museum,
				// 7 - 10 transportation, 10 - 13 concert
				Assert.assertEquals(start, map.get(new DateTime(0)));
				Assert.assertEquals(new Duration(4), map.get(new DateTime(1))
						.getDuration());
				Assert.assertEquals("museum",
						((Activity) (map.get(new DateTime(5)))).title);
				Assert.assertEquals(new Duration(3), map.get(new DateTime(7))
						.getDuration());
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
			Assert.assertEquals(new Duration(4), map.get(new DateTime(1))
					.getDuration());
			Assert.assertEquals("museum",
					((Activity) (map.get(new DateTime(5)))).title);
			Assert.assertEquals(new Duration(7), map.get(new DateTime(7))
					.getDuration());
			Assert.assertEquals("park",
					((Activity) (map.get(new DateTime(14)))).title);
			Assert.assertEquals(new Duration(2), map.get(new DateTime(15))
					.getDuration());
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
			// 20 - 25 transportation, 25 - 25 end
			Assert.assertEquals(start, map.get(new DateTime(0)));
			Assert.assertEquals(new Duration(4), map.get(new DateTime(1))
					.getDuration());
			Assert.assertEquals("museum",
					((Activity) (map.get(new DateTime(5)))).title);
			Assert.assertEquals(new Duration(7), map.get(new DateTime(7))
					.getDuration());
			Assert.assertEquals("park",
					((Activity) (map.get(new DateTime(14)))).title);
			Assert.assertEquals(new Duration(2), map.get(new DateTime(15))
					.getDuration());
			Assert.assertEquals("concert",
					((Activity) (map.get(new DateTime(17)))).title);
			Assert.assertEquals(new Duration(5), map.get(new DateTime(20))
					.getDuration());
			Assert.assertEquals(end, map.get(new DateTime(25)));
			Assert.assertEquals(new DateTime(25), child.getTb().lastEndTime());
			Assert.assertTrue(child.checkGoal());
			
		}
		
		successors = successors.get(0).successors();
		Assert.assertTrue(successors.isEmpty());
	}
	
	@Test
	public void testSuccessors2() {
		ArrayList<SearchState> successors = new ArrayList<SearchState>();
		
		SchedulingState beachFirst = null, templeFirst = null;
		
		// depth 1:
		// 1. start - beach
		// 2. start - temple
		successors = state2.successors();
		Assert.assertEquals(2, successors.size());
		for (int i = 0; i < successors.size(); i++) {
			SchedulingState child = (SchedulingState) successors.get(i);
			TreeMap<DateTime, Schedulable> map = child.getTb()
					.getScheduledActivities().getSchedule();
			switch ((int) child.getTb().lastEndTime().getMillis()) {
			case 13:
				// 7 - 7 start, 8 - 11 transportation, 11 - 13 beach
				Assert.assertEquals(start, map.get(new DateTime(7)));
				Assert.assertEquals(new Duration(3), map.get(new DateTime(8))
						.getDuration());
				Assert.assertEquals("beach",
						((Activity) (map.get(new DateTime(11)))).title);
				Assert.assertFalse(child.checkGoal());
				beachFirst = child;
				break;
			
			case 12:
				// 7 - 7 start, 8 - 9 transportation, 9 - 12 temple
				Assert.assertEquals(start, map.get(new DateTime(7)));
				Assert.assertEquals(new Duration(1), map.get(new DateTime(8))
						.getDuration());
				Assert.assertEquals("temple",
						((Activity) (map.get(new DateTime(9)))).title);
				templeFirst = child;
				Assert.assertFalse(child.checkGoal());
				break;
			
			default:
				fail("cannot find successor match");
				break;
			}
			Assert.assertEquals(2, child.getActivities().size());
			
		}
		
		// depth 2: beach first
		// no result
		successors = beachFirst.successors();
		Assert.assertEquals(0, successors.size());
		
		// depth 2: temple first
		// 1. start - temple - beach
		// 2. start - temple - gallery
		successors = templeFirst.successors();
		Assert.assertEquals(2, successors.size());
		for (int i = 0; i < successors.size(); i++) {
			SchedulingState child = (SchedulingState) successors.get(i);
			TreeMap<DateTime, Schedulable> map = child.getTb()
					.getScheduledActivities().getSchedule();
			switch ((int) child.getTb().lastEndTime().getMillis()) {
			case 16:
				// 7 - 7 start, 8 - 9 transportation, 9 - 12 temple
				// 12 - 14 transportation, 14 - 16 beach
				Assert.assertEquals(start, map.get(new DateTime(7)));
				Assert.assertEquals(new Duration(1), map.get(new DateTime(8))
						.getDuration());
				Assert.assertEquals("temple",
						((Activity) (map.get(new DateTime(9)))).title);
				Assert.assertEquals(new Duration(2), map.get(new DateTime(12))
						.getDuration());
				Assert.assertEquals("beach",
						((Activity) (map.get(new DateTime(14)))).title);
				Assert.assertFalse(child.checkGoal());
				break;
			
			case 21:
				// 7 - 7 start, 8 - 9 transportation, 9 - 12 temple
				// 12 - 17 transportation, 17 - 21 gallery
				Assert.assertEquals(start, map.get(new DateTime(7)));
				Assert.assertEquals(new Duration(1), map.get(new DateTime(8))
						.getDuration());
				Assert.assertEquals("temple",
						((Activity) (map.get(new DateTime(9)))).title);
				Assert.assertEquals(new Duration(5), map.get(new DateTime(12))
						.getDuration());
				Assert.assertEquals("gallery",
						((Activity) (map.get(new DateTime(17)))).title);
				Assert.assertFalse(child.checkGoal());
				break;
			
			}
			Assert.assertEquals(1, child.getActivities().size());
		}
		
	}
	
	@Test
	public void testSuccessors3() {
		ArrayList<SearchState> successors = new ArrayList<SearchState>();
		
		// depth 1:
		// 1. start - skiing
		// 2. start - hiking
		successors = state3.successors();
		Assert.assertEquals(2, successors.size());
		for (int i = 0; i < successors.size(); i++) {
			SchedulingState child = (SchedulingState) successors.get(i);
			TreeMap<DateTime, Schedulable> map = child.getTb()
					.getScheduledActivities().getSchedule();
			switch ((int) child.getTb().lastEndTime().getMillis()) {
			case 20:
				// 0 - 0 start, 1 - 3 transportation, 15 - 20 skiing
				Assert.assertEquals(start, map.get(new DateTime(0)));
				Assert.assertEquals(new Duration(2), map.get(new DateTime(1))
						.getDuration());
				Assert.assertEquals("skiing",
						((Activity) (map.get(new DateTime(15)))).title);
				Assert.assertFalse(child.checkGoal());
				break;
			
			case 10:
				// 0 - 0 start, 1 - 6 transportation, 6 - 10 hiking
				Assert.assertEquals(start, map.get(new DateTime(0)));
				Assert.assertEquals(new Duration(5), map.get(new DateTime(1))
						.getDuration());
				Assert.assertEquals("hiking",
						((Activity) (map.get(new DateTime(6)))).title);
				Assert.assertFalse(child.checkGoal());
				break;
			
			default:
				fail("cannot find successor match");
				break;
			}
			Assert.assertEquals(1, child.getActivities().size());
			
		}
		
	}
	
	@Test
	public void testSuccessors4() {
		ArrayList<SearchState> successors = new ArrayList<SearchState>();
		
		// create activities
		LegalTimeline legal1 = new LegalTimeline(new Interval(1, 10));
		Assert.assertTrue(legal1.schedule(1, 10));
		
		Activity swimming = new Activity("swimming", new Duration(2),
				new Location(7, 7), legal1);
		
		legal1 = new LegalTimeline(new Interval(1, 20));
		legal1.schedule(10, 15);
		Activity shower = new Activity("shower", new Duration(2), new Location(
				0, 0), legal1);
		Activity tv = new Activity("watch TV", new Duration(3), new Location(0,
				0), legal1);
		
		Set<Activity> activitiesSameLoc = new HashSet<Activity>();
		activitiesSameLoc.add(shower);
		activitiesSameLoc.add(tv);
		
		SchedulingState state4 = new SchedulingState(tb3, graph,
				activitiesSameLoc);
		state4.activities.add(swimming);
		state4.activities.add(shower);
		state4.activities.add(tv);
		
		// start - shower - swimming - tv - end
		successors = state4.successors();
		successors = successors.get(0).successors();
		successors = successors.get(0).successors();
		successors = successors.get(0).successors();
		Assert.assertEquals(1, successors.size());
		Debugger.printSchedulables(((SchedulingState) successors.get(0))
				.getTb());
		
	}
	
	public static void initHelper() {
		// building basics
		tb1 = new TimeBlock(1, new Interval(1, 30), new Location(0, 0),
				new Location(0, 0));
		tb2 = new TimeBlock(2, new Interval(8, 30), new Location(0, 0),
				new Location(0, 0));
		tb3 = new TimeBlock(3, new Interval(1, 25), new Location(0, 0),
				new Location(0, 0));
		start = new Activity("At start location", new Duration(0),
				new Location(0, 0));
		end = new Activity("At end location", new Duration(0), new Location(0,
				0));
		
		// building AST1 activities
		LegalTimeline legal1 = new LegalTimeline(new Interval(1, 30));
		Assert.assertTrue(legal1.schedule(new DateTime(1), new LegalTime(
				new Duration(20))));
		Activity museum = new Activity("museum", new Duration(2), new Location(
				1, 1), legal1);
		Activity concert = new Activity("concert", new Duration(3),
				new Location(2, 2), (LegalTimeline) DeepCopy.copy(legal1));
		Activity park = new Activity("park", new Duration(1),
				new Location(3, 3), (LegalTimeline) DeepCopy.copy(legal1));
		
		// building AST2 activities
		legal1 = new LegalTimeline(new Interval(6, 26));
		Assert.assertTrue(legal1.schedule(6, 26));
		Activity beach = new Activity("beach", new Duration(2), new Location(4,
				4), legal1);
		legal1 = new LegalTimeline(new Interval(13, 21));
		Assert.assertTrue(legal1.schedule(13, 21));
		Activity gallery = new Activity("gallery", new Duration(4),
				new Location(5, 5), legal1);
		legal1 = new LegalTimeline(new Interval(9, 16));
		Assert.assertTrue(legal1.schedule(9, 12));
		Assert.assertTrue(legal1.schedule(13, 16));
		Activity temple = new Activity("temple", new Duration(3), new Location(
				6, 6), legal1);
		
		// building AST3 activities
		legal1 = new LegalTimeline(new Interval(1, 23));
		Assert.assertTrue(legal1.schedule(1, 7));
		Assert.assertTrue(legal1.schedule(15, 23));
		Activity skiing = new Activity("skiing", new Duration(5), new Location(
				7, 7), legal1);
		legal1 = new LegalTimeline(new Interval(3, 30));
		Assert.assertTrue(legal1.schedule(3, 30));
		Activity hiking = new Activity("hiking", new Duration(4), new Location(
				8, 8), legal1);
		
		// building tb1
		ArrayList<TimeBlock> availableTBs = new ArrayList<TimeBlock>();
		availableTBs.add(tb1);
		HashSet<Activity> activities1 = new HashSet<Activity>();
		activities1.add(museum);
		activities1.add(concert);
		activities1.add(park);
		
		// building tb2
		availableTBs = new ArrayList<TimeBlock>();
		availableTBs.add(tb2);
		HashSet<Activity> activities2 = new HashSet<Activity>();
		activities2.add(beach);
		activities2.add(gallery);
		activities2.add(temple);
		
		// building tb3
		availableTBs = new ArrayList<TimeBlock>();
		availableTBs.add(tb3);
		HashSet<Activity> activities3 = new HashSet<Activity>();
		activities3.add(skiing);
		activities3.add(hiking);
		
		// build graph
		graph.addVertex(start.location);
		graph.addVertex(museum.location);
		graph.addVertex(concert.location);
		graph.addVertex(park.location);
		graph.addVertex(beach.location);
		graph.addVertex(gallery.location);
		graph.addVertex(temple.location);
		graph.addVertex(skiing.location);
		graph.addVertex(hiking.location);
		
		// the part for AST1
		graph.addEdge(museum.location, concert.location, new Transportation(
				new Duration(3)));
		graph.addEdge(park.location, museum.location, new Transportation(
				new Duration(7)));
		graph.addEdge(park.location, concert.location, new Transportation(
				new Duration(2)));
		graph.addEdge(start.location, concert.location, new Transportation(
				new Duration(5)));
		graph.addEdge(start.location, museum.location, new Transportation(
				new Duration(4)));
		graph.addEdge(start.location, park.location, new Transportation(
				new Duration(3)));
		
		// the part for AST2
		graph.addEdge(beach.location, temple.location, new Transportation(
				new Duration(2)));
		graph.addEdge(gallery.location, temple.location, new Transportation(
				new Duration(5)));
		graph.addEdge(beach.location, gallery.location, new Transportation(
				new Duration(3)));
		graph.addEdge(start.location, temple.location, new Transportation(
				new Duration(1)));
		graph.addEdge(start.location, gallery.location, new Transportation(
				new Duration(4)));
		graph.addEdge(start.location, beach.location, new Transportation(
				new Duration(3)));
		
		// the part for AST3
		graph.addEdge(skiing.location, hiking.location, new Transportation(
				new Duration(3)));
		graph.addEdge(start.location, skiing.location, new Transportation(
				new Duration(2)));
		graph.addEdge(start.location, hiking.location, new Transportation(
				new Duration(5)));
		
		state1 = new SchedulingState(tb1, graph, activities1);
		state2 = new SchedulingState(tb2, graph, activities2);
		state3 = new SchedulingState(tb3, graph, activities3);
		
	}
}
