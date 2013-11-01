package algo;
import java.util.ArrayList;

import search.AStar;
import search.TreeSearch;
import state.SchedulingState;
import time.TimeBlock;

public class Scheduler {

	public boolean scheduleAll(ArrayList<TimeBlock> timeblocks) {
		for (TimeBlock timeblock : timeblocks) {
			if (!schedule(timeblock)) {
				return false;
			}

		}
		return true;
	}

	public boolean schedule(TimeBlock timeblock) {

		// construct the initial state
		SchedulingState root = new SchedulingState(timeblock);

		TreeSearch searcher = new TreeSearch(new AStar(), root);

		if (searcher.nextGoal() != null) {
			return true;
		} else {
			return false;
		}
	}
}
