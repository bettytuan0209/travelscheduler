package state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import time.TimeBlock;
import util.DeepCopy;
import activities.ActivitySpanningTree;

public class MatchingState implements SearchState, Serializable {
	
	private static final long serialVersionUID = 4188017693668337015L;
	protected Set<ActivitySpanningTree> asts;
	private HashMap<TimeBlock, ActivitySpanningTree> matches;
	
	public MatchingState(Set<ActivitySpanningTree> asts,
			ArrayList<TimeBlock> tbs) {
		this.asts = asts;
		matches = new HashMap<TimeBlock, ActivitySpanningTree>();
		
		for (TimeBlock tb : tbs) {
			matches.put(tb, null);
		}
	}
	
	@Override
	public ArrayList<SearchState> successors() {
		ArrayList<SearchState> successors = new ArrayList<SearchState>();
		
		// handle the next ast in the set
		Iterator<ActivitySpanningTree> itr = asts.iterator();
		if (itr.hasNext()) {
			ActivitySpanningTree ast = itr.next();
			asts.remove(ast);
			
			// for each tb that this ast can match with
			for (TimeBlock tb : ast.getAvailableTBs(matches.keySet())) {
				
				// if this tb hasn't been matched with another ast
				if (matches.get(tb) == null) {
					
					// create a new state with that match and insert to
					// successors
					MatchingState newState = (MatchingState) DeepCopy
							.copy(this);
					
					if (newState.matches.put(tb, ast) == null) {
						successors.add(newState);
					} else {
						throw new IllegalStateException(
								"Internal inconsistency. Duplicate matching to the same TB");
					}
					
				}
			}
			
		} else {
			throw new IllegalStateException("Unexceptedly out of tree nodes");
		}
		
		return successors;
	}
	
	@Override
	public boolean checkGoal() {
		return asts.isEmpty();
	}
	
	public HashMap<TimeBlock, ActivitySpanningTree> getMatches() {
		return matches;
	}
	
}
