package state;

import java.util.ArrayList;

import time.TimeBlock;

public class SchedulingState implements SearchState {

	public SchedulingState(TimeBlock timeblock) {
		// initialize state based on timeblock given
	}

	@Override
	public ArrayList<SearchState> successors() {
		return new ArrayList<SearchState>();
	}

	@Override
	public boolean checkGoal() {
		// TODO Auto-generated method stub
		return false;
	}

}
