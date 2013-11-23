package search;

import state.SearchState;

/**
 * The generic tree search algorithm that can become different tree search
 * strategies with different StatesContainer. Examples include DFS, BFS, and
 * AStar
 * 
 * @author chiao-yutuan
 * 
 */
public class TreeSearch {
	
	private StatesContainer statesContainer;
	private int numExpanded;
	
	/**
	 * Constructor. Needs a StatesContainer object and an intiail search state
	 * 
	 * @param statesContainer
	 *            The container for the states. This will determine what kind of
	 *            tree search it is
	 * @param initial
	 *            The initial state to be inserted into the container at
	 *            construction time
	 */
	public TreeSearch(StatesContainer statesContainer, SearchState initial) {
		statesContainer.add(initial);
		this.statesContainer = statesContainer;
		numExpanded = 0;
	}
	
	/**
	 * Perform the tree search and find the next goal state
	 * 
	 * @return The next goal SearchState
	 */
	public SearchState nextGoal() {
		SearchState result = null;
		while (!statesContainer.isEmpty()) {
			SearchState toExpand = statesContainer.pop();
			numExpanded++;
			
			// goal check
			if (toExpand.checkGoal()) {
				return toExpand;
			}
			
			// expand and insert successors
			statesContainer.addAll(toExpand.successors());
			
		}
		
		return result;
	}
	
	public int getNumExpanded() {
		return numExpanded;
	}
	
}
