package state;

import java.util.ArrayList;

/**
 * An interface for all search states. It consists of the common methods that
 * state should guarantee for tree searches
 * 
 * @author chiao-yutuan
 * 
 */
public interface SearchState {
	
	/**
	 * Return the list of successors to this state
	 * 
	 * @return An arraylist of the successor search states
	 */
	public ArrayList<SearchState> successors();
	
	/**
	 * Checks if this state is a goal
	 * 
	 * @return True if this is a goal state, false otherwise
	 */
	public boolean checkGoal();
	
}
