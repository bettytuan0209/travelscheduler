package algo;

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

public class Scheduler {
	
	public boolean scheduleAll(
			SimpleWeightedGraph<Location, Transportation> graph,
			HashMap<TimeBlock, ActivitySpanningTree> pairs) {
		for (Map.Entry<TimeBlock, ActivitySpanningTree> pair : pairs.entrySet()) {
			if (!schedule(graph, pair.getKey(), pair.getValue())) {
				return false;
			}
			
		}
		return true;
	}
	
	public boolean schedule(
			SimpleWeightedGraph<Location, Transportation> wholeGraph,
			TimeBlock timeblock, ActivitySpanningTree ast) {
		
		// construct the initial state
		SchedulingState root = new SchedulingState(timeblock, wholeGraph,
				ast.getActivities());
		
		TreeSearch searcher = new TreeSearch(new AStar(), root);
		
		if (searcher.nextGoal() != null) {
			return true;
		} else {
			return false;
		}
	}
	
}
