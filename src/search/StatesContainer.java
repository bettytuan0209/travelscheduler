package search;

import java.util.Collection;

import state.SearchState;

/**
 * The interface for container of search states.
 * 
 * @author chiao-yutuan
 * 
 */
public interface StatesContainer {
	
	public boolean isEmpty();
	
	public SearchState pop();
	
	public boolean add(SearchState initial);
	
	public boolean addAll(Collection<? extends SearchState> states);
	
}
