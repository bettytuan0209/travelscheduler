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

		// schedule startLocation at the beginning of the interval
		Activity start = new Activity(new Duration(1), tb.getStartLocation());
		tb.scheduleEarliestFree(start);
	}

	@Override
	public ArrayList<SearchState> successors() {
		ArrayList<SearchState> successors = new ArrayList<SearchState>();

		// go through all unscheduled activities
		for (Activity activity : ast.getActivities()) {
			TimeBlock newTb = (TimeBlock) DeepCopy.copy(tb);
			ActivitySpanningTree newTree = (ActivitySpanningTree) DeepCopy
					.copy(ast);
			SchedulingState newState = new SchedulingState(newTb, newTree);
			// if you can schedule this activity meeting constraints,
			// add as a successor
			if (newTb.scheduleEarliestFree(activity)
					&& newTree.removeActivity(activity)
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
		DateTime earliestFree = tb.earliestFree();
		for (Activity activity : ast.getActivities()) {
			activity.setEarlistStartTime(earliestFree);
			if (!activity.enoughLegalTimes()) {
				return false;
			}

		}
		return true;
	}

	@Override
	public int compareTo(SchedulingState other) {
		return other.tb.earliestFree().plus(other.ast.getSumActivitiesTime())
				.compareTo(tb.earliestFree().plus(ast.getSumActivitiesTime()));

	}
}
