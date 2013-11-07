package state;

import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import time.TimeBlock;
import util.DeepCopy;
import activities.Activity;
import activities.ActivitySpanningTree;

public class SchedulingState implements SearchState,
		Comparable<SchedulingState> {
	private TimeBlock tb;
	private ActivitySpanningTree ast;
	
	public SchedulingState(TimeBlock tb, ActivitySpanningTree ast) {
		// create an initial state based on the paired TB and AST given
		this.tb = tb;
		this.ast = ast;
	}
	
	@Override
	public ArrayList<SearchState> successors() {
		ArrayList<SearchState> successors = new ArrayList<SearchState>();
		
		// if nothing scheduled yet, schedule startLocation
		Activity start = new Activity("At start location", new Duration(0),
				tb.getStartLocation());
		tb.scheduleAfter(start);
		
		// if out of activities, schedule to return to endLocation
		if (ast.getActivities().isEmpty()) {
			Activity end = new Activity("At end location", new Duration(1),
					tb.getEndLocation());
			tb.scheduleAfterTb(end);
			return successors;
		}
		
		// go through all unscheduled activities
		for (Activity activity : ast.getActivities()) {
			SchedulingState newState = (SchedulingState) DeepCopy.copy(this);
			// if you can schedule this activity meeting constraints,
			// add as a successor
			if (newState.tb.scheduleAfter(activity)
					&& newState.ast.removeActivity(activity)
					&& newState.forwardChecking()) {
				successors.add(newState);
				
			}
			
		}
		
		return successors;
	}
	
	@Override
	public boolean checkGoal() {
		return ast.getActivities().isEmpty();
	}
	
	private boolean forwardChecking() {
		// update each activity's legal time
		// and check if still have enough time to schedule it
		DateTime earliestFree = tb.lastEndTime();
		for (Activity activity : ast.getActivities()) {
			activity.setEarliestStartTime(earliestFree);
			if (!activity.enoughLegalTimes()) {
				return false;
			}
			
		}
		return true;
	}
	
	@Override
	public int compareTo(SchedulingState other) {
		return other.tb.lastEndTime().plus(other.ast.getSumActivitiesTime())
				.compareTo(tb.lastEndTime().plus(ast.getSumActivitiesTime()));
		
	}
}
