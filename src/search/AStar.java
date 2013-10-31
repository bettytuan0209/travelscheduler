package search;

import java.util.PriorityQueue;

import state.SearchState;

@SuppressWarnings("serial")
public class AStar extends PriorityQueue<SearchState> implements
		StatesContainer {

	@Override
	public SearchState pop() {
		return poll();
	}

}
