package state;

import java.util.ArrayList;

public interface SearchState {

	public ArrayList<SearchState> successors();

	public boolean checkGoal();

}
