package time;

import java.io.Serializable;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import schedulable.Activity;
import schedulable.Schedulable;
import activities.Location;

public class TimeBlock implements Serializable {
	private static final long serialVersionUID = -2954314114527434246L;
	private int index;
	private Location startLocation;
	private Location endLocation;
	private Timeline scheduledActivities;
	
	public TimeBlock(int index, Interval timespan, Location startLocation,
			Location endLocation) {
		this.index = index;
		this.startLocation = startLocation;
		this.endLocation = endLocation;
		this.scheduledActivities = new Timeline(new Interval(timespan));
		
	}
	
	public boolean schedule(DateTime startTime, Schedulable schedulable) {
		return scheduledActivities.schedule(startTime, schedulable);
	}
	
	public boolean scheduleAfter(DateTime startTime, Schedulable schedulable) {
		// try to schedule it
		if (schedulable instanceof Activity) {
			Activity activity = (Activity) schedulable;
			return scheduledActivities.scheduleAfter(startTime,
					activity.legalTimeline, activity);
		} else {
			return scheduledActivities.scheduleAfter(startTime, schedulable);
		}
	}
	
	public boolean scheduleAfter(Schedulable schedulable) {
		return scheduleAfter(new DateTime(0), schedulable);
	}
	
	public boolean scheduleBeforeTb(Activity activity) {
		// sanitation check for duration overflow
		if (activity.getDuration().getMillis() > 1) {
			return false;
		}
		
		DateTime beforeInterval = scheduledActivities.getInterval().getStart()
				.minus(1);
		if (!scheduledActivities.hasScheduleStart(beforeInterval)
				&& scheduledActivities.schedule.put(beforeInterval, activity) == null) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean scheduleAfterTb(Activity activity) {
		// sanitation check for duration overflow
		if (activity.getDuration().getMillis() > 1) {
			return false;
		}
		DateTime afterInterval = scheduledActivities.getInterval().getEnd();
		if (!scheduledActivities.hasScheduleStart(afterInterval)
				&& scheduledActivities.schedule.put(afterInterval, activity) == null) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TimeBlock) {
			TimeBlock other = (TimeBlock) obj;
			if (index == other.index
					&& startLocation.equals(other.startLocation)
					&& endLocation.equals(other.endLocation)
					&& scheduledActivities.equals(other.scheduledActivities)) {
				return true;
			}
			
		}
		return false;
	}
	
	public Interval getInterval() {
		return scheduledActivities.interval;
	}
	
	public DateTime lastEndTime() {
		return scheduledActivities.lastEndTime();
	}
	
	public Map.Entry<DateTime, Schedulable> getLastScheduled() {
		return scheduledActivities.schedule.lastEntry();
	}
	
	public int getIndex() {
		return index;
	}
	
	public Location getStartLocation() {
		return startLocation;
	}
	
	public Location getEndLocation() {
		return endLocation;
	}
	
	public Timeline getScheduledActivities() {
		return scheduledActivities;
	}
	
}