package activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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
 * A container for a set of activities.
 * 
 * @author chiao-yutuan
 * 
 */

public class ActivitySpanningTree implements Serializable {
	private static final long serialVersionUID = 6797165864508180241L;
	private int index;
	private ArrayList<TimeBlock> availableTBs;
	private Set<Activity> activities;
	private Duration sumActivitiesTime;
	
	/**
	 * Construct with an index and a list of available TBs
	 * 
	 * @param index
	 *            The index used to identify this AST
	 * @param tbs
	 *            The list of TBs that this AST should be able to pair with
	 */
	
	public ActivitySpanningTree(int index, ArrayList<TimeBlock> tbs) {
		this.index = index;
		this.availableTBs = tbs;
		activities = new HashSet<Activity>();
		sumActivitiesTime = new Duration(0);
		
	}
	
	/**
	 * Add a set of activities to the AST. This method will check each
	 * activity's available times and update the availableTBs list accordingly.
	 * The sum of activities duration will be updated
	 * 
	 * @param activities
	 *            The set of activities to add
	 * @return whether activities were added successfully. Method will return
	 *         false with the AST unchanged if adding these activities will
	 *         result in an empty list of availableTBs
	 */
	public boolean addActivities(Set<Activity> activities) {
		ActivitySpanningTree clone = (ActivitySpanningTree) DeepCopy.copy(this);
		Iterator<Activity> itr = activities.iterator();
		while (itr.hasNext()) {
			if (!clone.addActivity(itr.next())) {
				return false;
			}
		}
		this.availableTBs = clone.availableTBs;
		this.activities = clone.activities;
		this.sumActivitiesTime = clone.sumActivitiesTime;
		return true;
	}
	
	/**
	 * Add a single activity to the AST. This method will check this activity's
	 * available times and update the availableTBs list accordingly. The sum of
	 * activities duration will be updated accordingly
	 * 
	 * @param activity
	 *            The activity to add.
	 * @return Whether the activity was added successfully. Method will return
	 *         false with the AST unchanged if adding it will result in an empty
	 *         list of availableTBs
	 */
	public boolean addActivity(Activity activity) {
		
		// check for commonTBs
		ArrayList<TimeBlock> commonTBs = new ArrayList<TimeBlock>();
		for (TimeBlock tb : availableTBs) {
			TreeMap<DateTime, Schedulable> map = new TreeMap<DateTime, Schedulable>();
			Interval interval = tb.getInterval();
			map.put(new DateTime(interval.getStart()),
					new LegalTime(interval.toDuration()));
			Activity tester = new Activity("tester", activity.getDuration(),
					activity.legalTimeline.intersect(new LegalTimeline(map)));
			
			if (tester.forwardChecking()) {
				commonTBs.add(tb);
			}
		}
		if (commonTBs.isEmpty()) {
			return false;
		}
		
		activities.add(activity);
		availableTBs = commonTBs;
		sumActivitiesTime = sumActivitiesTime.plus(activity.getDuration());
		
		return true;
	}
	
	public Set<Activity> getActivities() {
		return activities;
	}
	
	public Duration getSumActivitiesTime() {
		return sumActivitiesTime;
	}
	
	public ArrayList<TimeBlock> getAvailableTBs() {
		return availableTBs;
	}
	
	public int getIndex() {
		return index;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ActivitySpanningTree) {
			ActivitySpanningTree other = (ActivitySpanningTree) obj;
			if (index == other.index && availableTBs.equals(other.availableTBs)
					&& activities.equals(other.activities)
					&& sumActivitiesTime.equals(other.sumActivitiesTime)) {
				return true;
			}
			
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(index).append(availableTBs)
				.append(activities).append(sumActivitiesTime).toHashCode();
	}
	
}
