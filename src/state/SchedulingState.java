package state;

import java.util.ArrayList;

public class SchedulingState implements SearchState {

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
