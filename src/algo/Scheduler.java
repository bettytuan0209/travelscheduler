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

public class Scheduler {
	
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
	
	private static TimeBlock autoSchedule(
			SimpleWeightedGraph<Location, Transportation> wholeGraph,
			TimeBlock timeblock, ActivitySpanningTree ast) {
		
		// construct the initial state
		SchedulingState root = new SchedulingState(timeblock, wholeGraph,
				ast.getActivities());
		
		TreeSearch searcher = new TreeSearch(new AStar(), root);
		
		SchedulingState state = ((SchedulingState) searcher.nextGoal());
		if (state != null) {
			return state.getTb();
		} else {
			return null;
		}
	}
	
}
