package algo;

import java.util.ArrayList;
import java.util.HashMap;

import org.jgrapht.graph.SimpleWeightedGraph;
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
		ActivitySpanningTree ast1 = new ActivitySpanningTree(1, availableTBs);
		Assert.assertTrue(ast1.addActivities(SchedulingStateTest.state1
				.getActivities()));
		pairs.put(SchedulingStateTest.tb1, ast1);
		
		// The second TB - AST pair
		availableTBs = new ArrayList<TimeBlock>();
		availableTBs.add(SchedulingStateTest.tb2);
		ActivitySpanningTree ast2 = new ActivitySpanningTree(2, availableTBs);
		Assert.assertTrue(ast2.addActivities(SchedulingStateTest.state2
				.getActivities()));
		pairs.put(SchedulingStateTest.tb2, ast2);
		
		// The third TB - AST pair
		availableTBs = new ArrayList<TimeBlock>();
		availableTBs.add(SchedulingStateTest.tb3);
		ActivitySpanningTree ast3 = new ActivitySpanningTree(3, availableTBs);
		Assert.assertTrue(ast3.addActivities(SchedulingStateTest.state3
				.getActivities()));
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
}
