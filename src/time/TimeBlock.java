package time;

import java.io.Serializable;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import schedulable.Activity;
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
	
	public boolean scheduleAfter(DateTime startTime, Activity activity) {
		// try to schedule it
		return scheduledActivities.scheduleAfter(startTime,
				activity.getLegalTimeline(), activity);
	}
	
	public boolean scheduleAfter(Activity activity) {
		return scheduleAfter(new DateTime(0), activity);
	}
	
	public boolean scheduleBeforeTb(Activity activity) {
		// sanitation check for duration overflow
		if (activity.getDuration().getMillis() > 1) {
			return false;
		}
		
		DateTime beforeInterval = scheduledActivities.getInterval().getStart()
				.minus(1);
		if (scheduledActivities.hasScheduleStart(beforeInterval)) {
			return false;
		} else {
			scheduledActivities.schedule(beforeInterval, activity);
			return true;
		}
	}
	
	public boolean scheduleAfterTb(Activity activity) {
		// sanitation check for duration overflow
		if (activity.getDuration().getMillis() > 1) {
			return false;
		}
		DateTime afterInterval = scheduledActivities.getInterval().getEnd()
				.plus(1);
		if (scheduledActivities.hasScheduleStart(afterInterval)) {
			return false;
		} else {
			scheduledActivities.schedule(afterInterval, activity);
			return true;
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
	
	public DateTime lastEndTime() {
		return scheduledActivities.lastEndTime();
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
	
}