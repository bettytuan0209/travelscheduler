package activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import schedulable.Activity;
import schedulable.LegalTime;
import schedulable.Schedulable;
import time.LegalTimeline;
import time.TimeBlock;
import util.DeepCopy;

public class ActivitySpanningTree implements Serializable {
	private static final long serialVersionUID = 6797165864508180241L;
	public int index;
	private ArrayList<TimeBlock> availableTBs;
	private Set<Activity> activities;
	private Duration sumActivitiesTime;
	
	public ActivitySpanningTree(int index, ArrayList<TimeBlock> tbs) {
		this.index = index;
		this.availableTBs = tbs;
		activities = new HashSet<Activity>();
		sumActivitiesTime = new Duration(0);
		
	}
	
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
	
}
