package search;

import state.SearchState;

public class TreeSearch {

	private StatesContainer states;
	private int numExpanded;

	public TreeSearch(SearchState root) {
		states.add(root);
		numExpanded = 0;

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

	public int getNumExpanded() {
		return numExpanded;
	}
}
