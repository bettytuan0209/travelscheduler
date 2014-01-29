package activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import schedulable.Activity;
import schedulable.LegalTime;
import schedulable.Schedulable;
import time.LegalTimeline;
import time.TimeBlock;
import util.DeepCopy;

/**
 * A spanning tree of a set of activities. It is represented as a set of
 * activities and a set of bridges (transportations) that connects them.
 * 
 * @author chiao-yutuan
 * 
 */

public class ActivitySpanningTree implements Serializable {
	private static final long serialVersionUID = 6797165864508180241L;
	private int index;
	private Set<Activity> activities;
	private Set<Bridge> bridges;
	
	/**
	 * Construct with an index and a list of available TBs
	 * 
	 * @param index
	 *            The index used to identify this AST
	 * @param tbs
	 *            The list of TBs that this AST should be able to pair with
	 */
	
	public ActivitySpanningTree(int index, Activity activity) {
		this.index = index;
		activities = new HashSet<Activity>();
		activities.add(activity);
		bridges = new HashSet<Bridge>();
		
	}
	
	/**
	 * Join two ASTs with a bridge. It will make a deep copy of this ast, add
	 * the other ast's activities and bridges to it, and add the bridge that
	 * connect them given as a parameter. Neither this or the other AST will be
	 * modified. This method does not check if the bridge actually connects the
	 * two ASTs
	 * 
	 * @param other
	 *            The other ast to join with
	 * @param bridge
	 *            The bridge that connects the two
	 * @return The AST that results from the join. Null is returned if
	 *         activities, bridges, or the bridge that connects two ASTs addAll
	 *         operation failed
	 */
	public ActivitySpanningTree joinAST(ActivitySpanningTree other,
			Bridge bridge) {
		
		ActivitySpanningTree union = (ActivitySpanningTree) DeepCopy.copy(this);
		
		if (union.activities.addAll(other.getActivities())
				&& (other.getBridges().size() == 0 || union.bridges
						.addAll(other.getBridges()))
				&& union.bridges.add(bridge)) {
			
			for (Activity activity : union.getActivities()) {
				System.out.print(activity.title);
			}
			System.out.println();
			
			return union;
		} else {
			return null;
		}
		
	}
	
	public ArrayList<TimeBlock> getAvailableTBs(Collection<TimeBlock> tbs) {
		return getAvailableTBs(tbs, false, false);
	}
	
	public ArrayList<TimeBlock> getAvailableTBs(Collection<TimeBlock> tbs,
			boolean checkActivitiesTime) {
		return getAvailableTBs(tbs, checkActivitiesTime, false);
	}
	
	/**
	 * Given list of TBs, return a sublist of TBs that all activities in this
	 * AST can fit in (aka for each activity, there exists some time segment on
	 * this TB with a duration >= to activity duration and overlaps with
	 * activity legal times. This doesn't guarantee that these activities won't
	 * overlap or put transportations into consideration
	 * 
	 * @param tbs
	 *            The list of tbs to filter from
	 * @return An arraylist of tbs that work
	 */
	public ArrayList<TimeBlock> getAvailableTBs(Collection<TimeBlock> tbs,
			boolean checkActivitiesTime, boolean checkTotalTime) {
		ArrayList<TimeBlock> commonTBs = new ArrayList<TimeBlock>();
		
		// Iterate through all TB candidates
		for (TimeBlock tb : tbs) {
			
			TreeMap<DateTime, Schedulable> map = new TreeMap<DateTime, Schedulable>();
			Interval interval = tb.getInterval();
			map.put(new DateTime(interval.getStart()),
					new LegalTime(interval.toDuration()));
			
			boolean allActivitiesFit = true;
			
			// check each activity in this AST if fit
			for (Activity activity : activities) {
				Activity tester = new Activity("tester",
						activity.getDuration(),
						activity.legalTimeline
								.intersect(new LegalTimeline(map)));
				
				if (!tester.forwardChecking()) {
					allActivitiesFit = false;
					break;
				}
			}
			
			// All activities in this AST fit in this TB, add to collection
			if (allActivitiesFit
					&& (!checkActivitiesTime || !tb.getInterval().toDuration()
							.isShorterThan(getSumActivitiesDuration()))
					&& (!checkTotalTime || !tb.getInterval().toDuration()
							.isShorterThan(getSumDuration()))) {
				commonTBs.add(tb);
			}
			
		}
		
		return commonTBs;
	}
	
	/**
	 * Remove all bridges in this AST. Used after clustering to release
	 * unnecessary memory
	 */
	public void clearBridges() {
		bridges.clear();
	}
	
	/**
	 * Calculate the sum of all activities, not putting bridges into
	 * consideration.
	 * 
	 * @return The sum as duration
	 */
	public Duration getSumActivitiesDuration() {
		Duration sum = new Duration(0);
		for (Activity activity : activities) {
			sum = sum.plus(activity.getDuration());
		}
		
		return sum;
	}
	
	/**
	 * Calculate the sum of all activities and bridges.
	 * 
	 * @return The sum as duration
	 */
	public Duration getSumDuration() {
		Duration sum = new Duration(0);
		for (Activity activity : activities) {
			sum = sum.plus(activity.getDuration());
		}
		for (Bridge bridge : bridges) {
			sum = sum.plus(bridge.getEdge().getDuration());
		}
		
		return sum;
	}
	
	public Set<Activity> getActivities() {
		return activities;
	}
	
	public Set<Bridge> getBridges() {
		return bridges;
	}
	
	public int getIndex() {
		return index;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ActivitySpanningTree) {
			ActivitySpanningTree other = (ActivitySpanningTree) obj;
			if (index == other.index && activities.equals(other.activities)
					&& bridges.equals(bridges)) {
				return true;
			}
			
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(index).append(activities)
				.append(bridges).toHashCode();
	}
	
	@Override
	public String toString() {
		String result = "Index: " + index + ". ";
		
		result += "Activities: ";
		for (Activity activity : activities) {
			result += activity.title + " ";
		}
		
		result += ". Bridges: ";
		for (Bridge bridge : bridges) {
			result += bridge.toString() + " ";
		}
		result += ". ";
		
		return result;
	}
}
