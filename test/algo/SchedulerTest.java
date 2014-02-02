package algo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.jgrapht.graph.SimpleWeightedGraph;
import org.joda.time.Duration;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import schedulable.Activity;
import schedulable.Transportation;
import search.TreeSearchTest;
import state.SchedulingStateTest;
import time.TimeBlock;
import util.Debugger;
import activities.ActivitySpanningTree;
import activities.Bridge;
import activities.Location;

public class SchedulerTest {
	static SimpleWeightedGraph<Location, Transportation> graph;
	static HashMap<TimeBlock, ActivitySpanningTree> pairs;
	
	@BeforeClass
	public static void init() {
		System.out
				.println("****************************************************");
		System.out
				.println("****************** Scheduler Test ******************");
		System.out
				.println("****************************************************");
		System.out.println();
		
		SchedulingStateTest.init();
		graph = SchedulingStateTest.graph;
		pairs = new HashMap<TimeBlock, ActivitySpanningTree>();
		
		// The first TB - AST pair
		ArrayList<TimeBlock> availableTBs = new ArrayList<TimeBlock>();
		availableTBs.add(SchedulingStateTest.tb1);
		ActivitySpanningTree ast1 = astWithActivities(1,
				SchedulingStateTest.state1.getActivities());
		pairs.put(SchedulingStateTest.tb1, ast1);
		
		// The second TB - AST pair
		availableTBs = new ArrayList<TimeBlock>();
		availableTBs.add(SchedulingStateTest.tb2);
		ActivitySpanningTree ast2 = astWithActivities(2,
				SchedulingStateTest.state2.getActivities());
		pairs.put(SchedulingStateTest.tb2, ast2);
		
		// The third TB - AST pair
		availableTBs = new ArrayList<TimeBlock>();
		availableTBs.add(SchedulingStateTest.tb3);
		ActivitySpanningTree ast3 = astWithActivities(3,
				SchedulingStateTest.state3.getActivities());
		pairs.put(SchedulingStateTest.tb3, ast3);
		
	}
	
	@Test
	public void testScheduleAll() {
		ArrayList<TimeBlock> tbs = Scheduler.autoScheduleAll(graph, pairs);
		Assert.assertNotNull(tbs);
		for (TimeBlock tb : tbs) {
			Debugger.printSchedulables(tb);
			System.out.println();
			switch (tb.getIndex()) {
			case 1:
				TreeSearchTest.testTb1Goal(tb);
				break;
			case 2:
				TreeSearchTest.testTb2Goal(tb);
				break;
			case 3:
				TreeSearchTest.testTb3Goal(tb);
				break;
			
			}
		}
		
	}
	
	@Test
	public void testNotMatched() {
		HashMap<TimeBlock, ActivitySpanningTree> notMatched = new HashMap<TimeBlock, ActivitySpanningTree>();
		Assert.assertNull(notMatched.put(SchedulingStateTest.tb1, null));
		ArrayList<TimeBlock> tbs = Scheduler.autoScheduleAll(graph, notMatched);
		Assert.assertEquals(1, tbs.size());
		Assert.assertEquals(SchedulingStateTest.tb1, tbs.get(0));
		Assert.assertEquals(0, tbs.get(0).getTimeline()
				.getSchedule().size());
	}
	
	private static ActivitySpanningTree astWithActivities(int index,
			Set<Activity> activities) {
		if (!activities.isEmpty()) {
			Iterator<Activity> itr = activities.iterator();
			ActivitySpanningTree ast = new ActivitySpanningTree(index,
					itr.next());
			
			while (itr.hasNext()) {
				Activity activity = itr.next();
				Bridge bridge = new Bridge(new Transportation(new Duration(0)),
						ast.getActivities().iterator().next(), activity);
				
				ActivitySpanningTree tmp = new ActivitySpanningTree(0, activity);
				ast = ast.joinAST(tmp, bridge);
			}
			
			return ast;
		}
		
		return null;
	}
}
