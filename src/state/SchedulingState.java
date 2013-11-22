package state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import schedulable.Activity;
import schedulable.Schedulable;
import schedulable.Transportation;
import time.TimeBlock;
import time.Timeline;
import util.DeepCopy;
import util.Util;
import activities.Location;

public class SchedulingState implements SearchState,
		Comparable<SchedulingState>, Serializable {
	private static final long serialVersionUID = -6014222737627795512L;
	private TimeBlock tb;
	private SimpleWeightedGraph<Location, Transportation> graph;
	public HashSet<Activity> activities;
	
	public SchedulingState(TimeBlock tb,
			SimpleWeightedGraph<Location, Transportation> graph,
			Set<Activity> activities) {
		// create an initial state based on the paired TB and AST given
		this.tb = tb;
		this.graph = graph;
		this.activities = new HashSet<Activity>(activities);
	}
	
	@Override
	public ArrayList<SearchState> successors() {
		
		ArrayList<SearchState> successors = new ArrayList<SearchState>();
		
		Timeline scheduledActivities = tb.getScheduledActivities();
		Activity start = new Activity("At start location", new Duration(0),
				tb.getStartLocation());
		Activity end = new Activity("At end location", new Duration(0),
				tb.getEndLocation());
		
		// if nothing scheduled yet, schedule startLocation
		if (scheduledActivities.isEmpty()
				&& !scheduledActivities.hasScheduleStart(scheduledActivities
						.getInterval().getStart().minus(1))) {
			
			tb.scheduleBeforeTb(start);
			
		}
		
		// if out of activities, schedule to return to endLocation
		if (activities.isEmpty()) {
			if (!tb.getScheduledActivities().hasScheduleStart(
					tb.getInterval().getEnd())) {
				SchedulingState newState = this.clone();
				Transportation edge = Util.searchTransportation(((Activity) tb
						.getLastScheduled().getValue()).location, end.location);
				if (edge != null
						&& newState.tb.schedule(tb.lastEndTime(), edge)
						&& newState.tb.scheduleAfterTb(end)) {
					
					successors.add(newState);
				}
			}
			return successors;
			
		}
		
		// go through all unscheduled activities
		for (Activity activity : activities) {
			SchedulingState newState = this.clone();
			
			// find last activity
			Map.Entry<DateTime, Schedulable> last = tb.getLastScheduled();
			Transportation edge;
			
			// find transportation
			if (last.getValue().equals(start)) {
				edge = Util.searchTransportation(start.location,
						activity.location);
			} else {
				edge = graph.getEdge(((Activity) last.getValue()).location,
						activity.location);
			}
			// if you can schedule this activity meeting constraints,
			// add as a successor
			if (edge != null
					&& newState.tb.scheduleAfter(newState.tb.lastEndTime(),
							edge)
					&& newState.tb.scheduleAfter(newState.tb.lastEndTime(),
							activity) && newState.activities.remove(activity)
					&& newState.forwardChecking()) {
				
				successors.add(newState);
				
			}
			
		}
		
		return successors;
	}
	
	@Override
	public boolean checkGoal() {
		Schedulable lastScheduled = tb.getLastScheduled().getValue();
		
		if (activities.isEmpty() && lastScheduled instanceof Activity) {
			Activity activity = ((Activity) lastScheduled);
			return activity.location != null
					&& activity.location.equals(tb.getEndLocation());
		}
		return false;
	}
	
	private boolean forwardChecking() {
		// update each activity's legal time
		// and check if still have enough time to schedule it
		DateTime earliestFree = tb.lastEndTime();
		for (Activity activity : activities) {
			activity.legalTimeline.setEarliestAvailable(earliestFree);
			if (!activity.forwardChecking()) {
				return false;
			}
			
		}
		return true;
	}
	
	@Override
	public int compareTo(SchedulingState other) {
		return tb
				.lastEndTime()
				.plus(sumActivitiesTime())
				.compareTo(
						other.tb.lastEndTime().plus(other.sumActivitiesTime()));
		
	}
	
	private Duration sumActivitiesTime() {
		Duration sum = new Duration(0);
		for (Activity current : activities) {
			sum = sum.plus(current.getDuration());
		}
		return sum;
	}
	
	public TimeBlock getTb() {
		return tb;
	}
	
	public SimpleWeightedGraph<Location, Transportation> getGraph() {
		return graph;
	}
	
	public Set<Activity> getActivities() {
		return activities;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SchedulingState) {
			SchedulingState other = (SchedulingState) obj;
			
			if (tb.equals(other.tb)
					&& graph.vertexSet().equals(other.graph.vertexSet())
					&& graph.edgeSet().equals(other.graph.edgeSet())
					&& activities.equals(other.activities)) {
				return true;
			}
			
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		
		return new HashCodeBuilder().append(tb).append(graph.vertexSet())
				.append(graph.edgeSet()).append(activities).toHashCode();
		
	}
	
	@Override
	public SchedulingState clone() {
		TimeBlock tbClone = (TimeBlock) DeepCopy.copy(tb);
		HashSet<Activity> activitiesClone = (HashSet<Activity>) DeepCopy
				.copy(activities);
		SchedulingState clone = new SchedulingState(tbClone, graph,
				activitiesClone);
		
		return clone;
	}
	
}
