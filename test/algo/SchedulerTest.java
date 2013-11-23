package algo;

import java.util.ArrayList;
import java.util.HashMap;

import org.jgrapht.graph.SimpleWeightedGraph;
import org.joda.time.Duration;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import schedulable.Transportation;
import search.TreeSearchTest;
import state.SchedulingStateTest;
import time.TimeBlock;
import util.Debugger;
import activities.ActivitySpanningTree;
import activities.Location;

public class SchedulerTest {
	static SimpleWeightedGraph<Location, Transportation> graph;
	static HashMap<TimeBlock, ActivitySpanningTree> pairs;
	
	@BeforeClass
	public static void init() {
		SchedulingStateTest.init();
		graph = SchedulingStateTest.graph;
		pairs = new HashMap<TimeBlock, ActivitySpanningTree>();
		
		// The first TB - AST pair
		ArrayList<TimeBlock> availableTBs = new ArrayList<TimeBlock>();
		availableTBs.add(SchedulingStateTest.tb1);
		ActivitySpanningTree ast1 = new ActivitySpanningTree(1, availableTBs,
				SchedulingStateTest.state1.getActivities(), new Duration(7));
		pairs.put(SchedulingStateTest.tb1, ast1);
		
		// The second TB - AST pair
		availableTBs = new ArrayList<TimeBlock>();
		availableTBs.add(SchedulingStateTest.tb2);
		ActivitySpanningTree ast2 = new ActivitySpanningTree(2, availableTBs,
				SchedulingStateTest.state2.getActivities(), new Duration(9));
		pairs.put(SchedulingStateTest.tb2, ast2);
		
	}
	
	@Test
	public void testScheduleAll() {
		ArrayList<TimeBlock> tbs = Scheduler.autoScheduleAll(graph, pairs);
		Assert.assertNotNull(tbs);
		for (TimeBlock tb : tbs) {
			Debugger.printSchedulables(tb.getScheduledActivities());
			System.out.println();
			switch (tb.getIndex()) {
			case 1:
				TreeSearchTest.testTb1Goal(tb);
				break;
			case 2:
				TreeSearchTest.testTb2Goal(tb);
				break;
			
			}
		}
		
	}
}
