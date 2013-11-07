package search;

import state.SearchState;
import state.StatesContainer;

public class TreeSearch {

	private StatesContainer states;
	private int numExpanded;

	public TreeSearch(StatesContainer states, SearchState initial) {
		states.add(initial);
		this.states = states;
		numExpanded = 0;
	}

	public int getNumExpanded() {
		return numExpanded;
	}

	public SearchState nextGoal() {
		SearchState result = null;
		while (!states.isEmpty()) {
			SearchState toExpand = states.pop();
			numExpanded++;

			// goal check
			if (toExpand.checkGoal()) {
				return toExpand;
			}

			// expand and insert successors
			states.addAll(toExpand.successors());

		}

		return result;
	}

}
