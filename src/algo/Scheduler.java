package algo;

import java.util.HashMap;
import java.util.Map;

import search.AStar;
import search.TreeSearch;
import state.SchedulingState;
import time.TimeBlock;
import activities.ActivitySpanningTree;

public class Scheduler {

	public boolean scheduleAll(HashMap<TimeBlock, ActivitySpanningTree> pairs) {
		for (Map.Entry<TimeBlock, ActivitySpanningTree> pair : pairs.entrySet()) {
			if (!schedule(pair.getKey(), pair.getValue())) {
				return false;
			}

		}
		return true;
	}

	public boolean schedule(TimeBlock timeblock, ActivitySpanningTree ast) {

		// construct the initial state
		SchedulingState root = new SchedulingState(timeblock, ast);

		TreeSearch searcher = new TreeSearch(new AStar(), root);

		if (searcher.nextGoal() != null) {
			return true;
		} else {
			return false;
		}
	}
}
