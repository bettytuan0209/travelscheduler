package search;

import java.util.PriorityQueue;

import state.SearchState;
import state.StatesContainer;

@SuppressWarnings("serial")
public class AStar extends PriorityQueue<SearchState> implements
		StatesContainer {

	@Override
	public SearchState pop() {
		return poll();
	}

}
