package algo;

import java.util.ArrayList;
import java.util.Set;

import org.jgrapht.graph.SimpleWeightedGraph;

import schedulable.Transportation;
import search.DFS;
import search.TreeSearch;
import state.MatchingState;
import time.TimeBlock;
import activities.ActivitySpanningTree;
import activities.Location;

public class ASTTBMatcher {
	
	public static ArrayList<TimeBlock> matching(
			SimpleWeightedGraph<Location, Transportation> graph,
			Set<ActivitySpanningTree> asts) {
		
		// construct the initial state
		MatchingState root = new MatchingState(asts);
		
		TreeSearch searcher = new TreeSearch(new DFS(), root);
		
		// for each goal state, pass to next module and wait for response
		MatchingState goal;
		while ((goal = (MatchingState) searcher.nextGoal()) != null) {
			ArrayList<TimeBlock> schedule = Scheduler.autoScheduleAll(graph,
					goal.getMatches());
			if (schedule != null) {
				return schedule;
			}
		}
		
		return null;
	}
}
