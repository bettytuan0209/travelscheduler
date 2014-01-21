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

/**
 * Automatically matches each AST with a TB
 * 
 * @author chiao-yutuan
 * 
 */
public class ASTTBMatcher {
	
	/**
	 * Matches each AST with a TB in its availableTBs list, then pass to
	 * scheduler. If scheduler failed, try to find the next possible match
	 * config
	 * 
	 * @param graph
	 *            The graph to pass on to the scheduler
	 * @param asts
	 *            Set of asts to match with. Their data structure contains the
	 *            availableTBs
	 * @return Result of the scheduler as an arraylist of timeblocks
	 */
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
