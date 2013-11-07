package state;

import java.util.Collection;

public interface StatesContainer {

	public boolean isEmpty();

	public SearchState pop();

	public boolean add(SearchState initial);

	public boolean addAll(Collection<? extends SearchState> states);

}
