package algo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.graph.SimpleWeightedGraph;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import schedulable.Activity;
import schedulable.Transportation;
import state.MatchingStateTest;
import time.LegalTimeline;
import time.TimeBlock;
import activities.ActivitySpanningTree;
import activities.Location;

public class ASTTBMatcherTest {
	
	@BeforeClass
	public static void init() {
		System.out
				.println("**************************************************");
		System.out
				.println("****************** Matcher Test ******************");
		System.out
				.println("**************************************************");
		System.out.println();
		
	}
	
	/**
	 * First result from matcher success in scheduling
	 */
	@Test
	public void testMatching1() {
		MatchingStateTest.initHelper();
		Set<ActivitySpanningTree> asts = MatchingStateTest.asts;
		ArrayList<TimeBlock> schedule = ASTTBMatcher.matching(
				new SimpleWeightedGraph<Location, Transportation>(
						Transportation.class), asts, MatchingStateTest.tbs);
		System.out.println(schedule.get(0));
		System.out.println(schedule.get(1));
		System.out.println(schedule.get(2));
		
	}
	
	/**
	 * First result from matcher fails to schedule. Second succeed
	 */
	@Test
	public void testMatching2() {
		// build TBs
		TimeBlock tb1 = new TimeBlock(1, new Interval(1, 10),
				new Location(0, 0), new Location(0, 0));
		TimeBlock tb2 = new TimeBlock(2, new Interval(15, 40), new Location(0,
				0), new Location(0, 0));
		
		ArrayList<TimeBlock> availableTBs = new ArrayList<TimeBlock>();
		Assert.assertTrue(availableTBs.add(tb1));
		Assert.assertTrue(availableTBs.add(tb2));
		
		// configure AST1
		LegalTimeline legal1 = new LegalTimeline(new Interval(1, 40));
		Assert.assertTrue(legal1.schedule(1, 40));
		Activity skiing = new Activity("skiing", new Duration(3), new Location(
				5, 5), legal1);
		ActivitySpanningTree ast1 = new ActivitySpanningTree(1, skiing);
		
		// configure AST2
		legal1 = new LegalTimeline(new Interval(9, 20));
		Assert.assertTrue(legal1.schedule(9, 20));
		Activity tv = new Activity("watch TV", new Duration(1), new Location(0,
				0), legal1);
		ActivitySpanningTree ast2 = new ActivitySpanningTree(2, tv);
		
		// configure graph
		SimpleWeightedGraph<Location, Transportation> graph = new SimpleWeightedGraph<Location, Transportation>(
				Transportation.class);
		Location hotel = new Location(0, 0);
		Assert.assertTrue(graph.addVertex(hotel));
		Assert.assertTrue(graph.addVertex(skiing.location));
		Assert.assertTrue(graph.addEdge(hotel, skiing.location,
				new Transportation(new Duration(5))));
		
		// Try matcher
		Set<ActivitySpanningTree> asts = new HashSet<ActivitySpanningTree>();
		Assert.assertTrue(asts.add(ast1));
		Assert.assertTrue(asts.add(ast2));
		ArrayList<TimeBlock> schedule = ASTTBMatcher.matching(graph, asts,
				availableTBs);
		Assert.assertNotNull(schedule);
		
		// Check TB2
		Assert.assertEquals(2, schedule.get(0).getIndex());
		Assert.assertEquals(skiing.title, ((Activity) (schedule.get(0)
				.getTimeline().getSchedule().get(new DateTime(20)))).title);
		
		// Check TB1
		Assert.assertEquals(1, schedule.get(1).getIndex());
		Assert.assertEquals(tv.title, ((Activity) (schedule.get(1)
				.getTimeline().getSchedule().get(new DateTime(9)))).title);
		
	}
	
}
