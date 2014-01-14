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
	private Set<ActivitySpanningTree> asts;
	private HashMap<TimeBlock, ActivitySpanningTree> matches;
	
	public MatchingState(Set<ActivitySpanningTree> asts) {
		this.asts = asts;
		matches = new HashMap<TimeBlock, ActivitySpanningTree>();
	}
	
	@Override
	public ArrayList<SearchState> successors() {
		ArrayList<SearchState> successors = new ArrayList<SearchState>();
		
		// handle the next ast in the set
		Iterator<ActivitySpanningTree> itr = asts.iterator();
		if (itr.hasNext()) {
			ActivitySpanningTree ast = itr.next();
			asts.remove(ast);
			
			System.out.println("Working on ast " + ast.getIndex());
			
			// for each tb that this ast can match with
			for (TimeBlock tb : ast.getAvailableTBs()) {
				System.out.println("Considering " + tb.getIndex());
				
				// if this tb hasn't been matched with another ast
				if (!matches.containsKey(tb)) {
					
					// create a new state with that match and insert to
					// successors
					MatchingState newState = (MatchingState) DeepCopy
							.copy(this);
					
					if (newState.matches.put(tb, ast) == null) {
						successors.add(newState);
						System.out.println("added a new state");
					} else {
						throw new IllegalStateException(
								"Internal inconsistency. Duplicate matching to the same TB");
					}
					
				}
			}
			System.out.println();
			
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
