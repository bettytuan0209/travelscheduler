package search;

import java.util.PriorityQueue;

import state.SearchState;

/**
 * A wrapper around PriorityQueue to implement AStar that meets the contract of
 * SearchContainer
 * 
 * @author chiao-yutuan
 * 
 */
@SuppressWarnings("serial")
public class AStar extends PriorityQueue<SearchState> implements
		StatesContainer {
	
	@Override
	public SearchState pop() {
		return poll();
	}
	
}
