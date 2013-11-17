package state;

import java.util.ArrayList;
import java.util.Map;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import schedulable.Activity;
import schedulable.Schedulable;
import schedulable.Transportation;
import time.TimeBlock;
import util.DeepCopy;
import util.Util;

public class SchedulingState implements SearchState,
		Comparable<SchedulingState> {
	private TimeBlock tb;
	private DirectedWeightedMultigraph<Activity, Transportation> graph;
	
	public SchedulingState(TimeBlock tb,
			DirectedWeightedMultigraph<Activity, Transportation> graph) {
		// create an initial state based on the paired TB and AST given
		this.tb = tb;
		this.graph = graph;
	}
	
	@Override
	public ArrayList<SearchState> successors() {
		ArrayList<SearchState> successors = new ArrayList<SearchState>();
		
		// if nothing scheduled yet, schedule startLocation
		Activity start = new Activity("At start location", new Duration(0),
				tb.getStartLocation());
		tb.scheduleAfter(start);
		
		// if out of activities, schedule to return to endLocation
		if (graph.vertexSet().isEmpty()) {
			Activity end = new Activity("At end location", new Duration(1),
					tb.getEndLocation());
			tb.scheduleAfterTb(end);
			return successors;
		}
		
		// go through all unscheduled activities
		for (Activity activity : graph.vertexSet()) {
			SchedulingState newState = (SchedulingState) DeepCopy.copy(this);
			
			// find last activity
			Map.Entry<DateTime, Schedulable> last = tb.getLastScheduled();
			Transportation edge = graph.getEdge((Activity) last.getValue(),
					activity);
			
			// if you can schedule this activity meeting constraints,
			// add as a successor
			if (newState.tb.scheduleAfter(
					Util.getEndTime(last).plus(edge.getDuration()), activity)
					&& newState.graph.removeVertex(activity)
					&& newState.forwardChecking()
					&& newState.tb.schedule(tb.lastEndTime(), edge)) {
				
				successors.add(newState);
				
			}
			
		}
		
		return successors;
	}
	
	@Override
	public boolean checkGoal() {
		return graph.vertexSet().isEmpty();
	}
	
	private boolean forwardChecking() {
		// update each activity's legal time
		// and check if still have enough time to schedule it
		DateTime earliestFree = tb.lastEndTime();
		for (Activity activity : graph.vertexSet()) {
			activity.legalTimeline.setEarliestAvailable(earliestFree);
			if (!activity.forwardChecking()) {
				return false;
			}
			
		}
		return true;
	}
	
	@Override
	public int compareTo(SchedulingState other) {
		return other.tb.lastEndTime().plus(other.sumActivitiesTime())
				.compareTo(tb.lastEndTime().plus(sumActivitiesTime()));
		
	}
	
	public Duration sumActivitiesTime() {
		return new Duration(0);
	}
}
