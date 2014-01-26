package algo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jgrapht.graph.SimpleWeightedGraph;

import schedulable.Transportation;
import search.AStar;
import search.TreeSearch;
import state.SchedulingState;
import time.TimeBlock;
import activities.ActivitySpanningTree;
import activities.Location;

/**
 * Automatic scheduler of activities
 * 
 * @author chiao-yutuan
 * 
 */

public class Scheduler {
	/**
	 * Iterate through a list of TB - AST pairs and automatically schedule each.
	 * 
	 * @param graph
	 *            The graph that contains the transportation details between all
	 *            locations. The graph is shared by all TB - AST pairs.
	 * @param pairs
	 *            HashMap of TB - AST pairs, meaning to schedule the activities
	 *            in the AST to its paired TB
	 * @return a list of TBs with activities scheduled in returns null if at
	 *         least one TB - AST pair failed to schedule
	 */
	public static ArrayList<TimeBlock> autoScheduleAll(
			SimpleWeightedGraph<Location, Transportation> graph,
			HashMap<TimeBlock, ActivitySpanningTree> pairs) {
		
		ArrayList<TimeBlock> autoSchedules = new ArrayList<TimeBlock>();
		
		for (Map.Entry<TimeBlock, ActivitySpanningTree> pair : pairs.entrySet()) {
			TimeBlock autoSchedule = autoSchedule(graph, pair.getKey(),
					pair.getValue());
			if (autoSchedule == null) {
				return null;
			} else {
				autoSchedules.add(autoSchedule);
			}
			
		}
		return autoSchedules;
	}
	
	/**
	 * Automatically schedule one single TB - AST pair.
	 * 
	 * @param wholeGraph
	 *            graph for this entire trip
	 * @param timeblock
	 *            the TB to be scheduled on
	 * @param ast
	 *            The AST of activities to schedule in this TB
	 * @return The timeblock that has activities scheduled in
	 */
	private static TimeBlock autoSchedule(
			SimpleWeightedGraph<Location, Transportation> wholeGraph,
			TimeBlock timeblock, ActivitySpanningTree ast) {
		
		// If the TB isn't matched with any AST, return it as is
		if (ast == null) {
			return timeblock;
		}
		
		// construct the initial state
		SchedulingState root = new SchedulingState(timeblock, wholeGraph,
				ast.getActivities());
		
		TreeSearch searcher = new TreeSearch(new AStar(), root);
		
		// We only need the most optimal solution since it's the last part of
		// the workflow
		SchedulingState goal = ((SchedulingState) searcher.nextGoal());
		if (goal != null) {
			return goal.getTb();
		} else {
			return null;
		}
	}
	
}
