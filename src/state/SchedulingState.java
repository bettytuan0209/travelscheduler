package state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

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
import activities.Location;

/**
 * This is the state that the Scheduler uses to schedule a set of activities in
 * an AST to a timeline in a TB. A state consists of the TB related to this
 * scheduling procedure, the graph with location and transportation information
 * between locations and a set of activities to schedule
 * 
 * @author chiao-yutuan
 * 
 */
public class SchedulingState implements SearchState,
		Comparable<SchedulingState>, Serializable {
	private static final long serialVersionUID = -6014222737627795512L;
	private TimeBlock tb;
	private SimpleWeightedGraph<Location, Transportation> graph;
	public HashSet<Activity> activities;
	
	/**
	 * Constructor with all fields. TB and the graph are taken by reference. The
	 * set of activities is recreated
	 * 
	 * @param tb
	 *            The TB to schedule on
	 * @param graph
	 *            The graph with location and transporation informations
	 * @param activities
	 *            The set of activities yet to schedule
	 */
	public SchedulingState(TimeBlock tb,
			SimpleWeightedGraph<Location, Transportation> graph,
			Set<Activity> activities) {
		// create an initial state based on the paired TB and AST given
		this.tb = tb;
		this.graph = graph;
		this.activities = new HashSet<Activity>(activities);
	}
	
	/**
	 * Overrides the SearchState's successors(). Generates the list of
	 * successors from this state
	 * 
	 * @return an ArrayList of search states
	 */
	
	@Override
	public ArrayList<SearchState> successors() {
		ArrayList<SearchState> successors = new ArrayList<SearchState>();
		
		Timeline scheduledActivities = tb.getTimeline();
		
		// if nothing scheduled yet, schedule startLocation
		if (scheduledActivities.isEmpty()
				&& !scheduledActivities.hasScheduleStart(scheduledActivities
						.getInterval().getStart().minus(1))) {
			Activity start = new Activity("At start location", new Duration(0),
					tb.getStartLocation());
			
			tb.scheduleBeforeTb(start);
			
		}
		
		// if out of activities, schedule to return to endLocation
		if (activities.isEmpty()) {
			Activity end = new Activity("At end location", new Duration(0),
					tb.getEndLocation());
			
			TreeMap<DateTime, Schedulable> schedule = tb
					.getTimeline().getSchedule();
			Schedulable last = schedule.lastEntry().getValue();
			if (last instanceof Activity) {
				SchedulingState newState = this.clone();
				Transportation edge;
				if (!((Activity) last).location.equals(tb.getEndLocation())) {
					edge = graph.getEdge(((Activity) tb.getLastScheduled()
							.getValue()).location, end.location);
					if (edge != null
							&& newState.tb
									.scheduleAfter(tb.lastEndTime(), edge)
							&& newState.tb.scheduleAfterTb(end)) {
						
						successors.add(newState);
					}
				} else if (!last.equals(end)) {
					if (newState.tb.scheduleAfterTb(end)) {
						
						successors.add(newState);
					}
				}
			}
			return successors;
			
		}
		
		// go through all unscheduled activities
		for (Activity activity : activities) {
			SchedulingState newState = this.clone();
			
			// find last activity
			Schedulable last = tb.getLastScheduled().getValue();
			
			Transportation edge;
			// if the two activities have different locations
			if (!((Activity) last).location.equals(activity.location)) {
				edge = graph.getEdge(((Activity) last).location,
						activity.location);
				
				// if you can schedule this activity meeting constraints,
				// add as a successor
				if (edge != null
						&& newState.tb.scheduleAfter(newState.tb.lastEndTime(),
								edge)
						&& newState.tb.scheduleAfter(newState.tb.lastEndTime(),
								activity)
						&& newState.activities.remove(activity)
						&& newState.forwardChecking()) {
					
					successors.add(newState);
					
				}
			} else { // if two activities are at the same location
				if (newState.tb.scheduleAfter(newState.tb.lastEndTime(),
						activity)
						&& newState.activities.remove(activity)
						&& newState.forwardChecking()) {
					successors.add(newState);
					
				}
			}
			
		}
		
		return successors;
	}
	
	/**
	 * Overrides the SearchState's checkGoal(). A SchedulingState is a goal
	 * state if it doesn't have any unscheduled activities left and the last
	 * activity scheduled is at endLocation
	 * 
	 * @return True if this is a goal state. False if otherwise
	 */
	@Override
	public boolean checkGoal() {
		
		if (activities.isEmpty() && tb.getLastScheduled() != null) {
			Schedulable lastScheduled = tb.getLastScheduled().getValue();
			if (lastScheduled instanceof Activity) {
				Activity activity = ((Activity) lastScheduled);
				return activity.location != null
						&& activity.location.equals(tb.getEndLocation());
			}
		}
		return false;
	}
	
	/**
	 * Helper function that performs forward checking for successors(). It goes
	 * through all unscheduled activities, update their legaltime to reflect the
	 * last scheduled and see if there is still enough legal time to schedule
	 * this unscheduled activity
	 * 
	 * @return True if all unscheduled activities returned true for their forwar
	 *         checks. This method can return false if at least one activity
	 *         didn't pass their forward check or setEarliestAvailable()
	 *         returned false
	 */
	private boolean forwardChecking() {
		// update each activity's legal time
		// and check if still have enough time to schedule it
		DateTime earliestFree = tb.lastEndTime();
		for (Activity activity : activities) {
			if (activity.legalTimeline.setEarliestAvailable(earliestFree)
					&& !activity.forwardChecking()) {
				return false;
			}
			
		}
		return true;
	}
	
	/**
	 * Implements compareTo() for the interface Comparable so that states with
	 * the lowest h value is placed on top of the priority queue. G value is the
	 * last end time of the TB. H value is the sum of all unscheduled activities
	 * 
	 * @param other
	 *            The SchedulingState to compare with
	 * 
	 * @return A negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 */
	
	public int compareTo(SchedulingState other) {
		
		return tb
				.lastEndTime()
				.plus(sumActivitiesTime())
				.compareTo(
						other.tb.lastEndTime().plus(other.sumActivitiesTime()));
		
	}
	
	/**
	 * Helper function that adds the duration of all unscheduled activities
	 * together
	 * 
	 * @return The sum duration of all unscheduled activities
	 */
	private Duration sumActivitiesTime() {
		Duration sum = new Duration(0);
		for (Activity current : activities) {
			sum = sum.plus(current.getDuration());
		}
		return sum;
	}
	
	/**
	 * Overrides the object equals() method. Checks all fields to see if
	 * equals(). Consistent with hashCode()
	 */
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
	
	/**
	 * Overrides the object hashCode() method. Creates a hash using all fields
	 * in the class. Consistent with equals()
	 */
	@Override
	public int hashCode() {
		
		return new HashCodeBuilder().append(tb).append(graph.vertexSet())
				.append(graph.edgeSet()).append(activities).toHashCode();
		
	}
	
	/**
	 * Overrides the object clone() method. This method creates a deep copy of
	 * this state except the graph, which is passed by reference, because it is
	 * rarely necessary to clone a copy of the graph
	 * 
	 * @return the copy of the state
	 */
	@Override
	public SchedulingState clone() {
		TimeBlock tbClone = (TimeBlock) DeepCopy.copy(tb);
		@SuppressWarnings("unchecked")
		HashSet<Activity> activitiesClone = (HashSet<Activity>) DeepCopy
				.copy(activities);
		SchedulingState clone = new SchedulingState(tbClone, graph,
				activitiesClone);
		
		return clone;
	}
	
	/************************** Getters ***************************/
	
	public TimeBlock getTb() {
		return tb;
	}
	
	public SimpleWeightedGraph<Location, Transportation> getGraph() {
		return graph;
	}
	
	public Set<Activity> getActivities() {
		return activities;
	}
	
}
