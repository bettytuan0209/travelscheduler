package time;

import java.io.Serializable;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import activities.Activity;
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
		scheduledActivities = new Timeline(timespan);
	}
	
	public boolean scheduleAfter(DateTime startTime, Activity activity) {
		// try to schedule it
		return scheduledActivities.scheduleAfter(startTime, activity);
	}
	
	public boolean scheduleAfter(Activity activity) {
		return scheduleAfter(new DateTime(0), activity);
	}
	
	public DateTime lastEndTime() {
		return scheduledActivities.lastEndTime();
	}
	
	public Location getStartLocation() {
		return startLocation;
	}
	
	public Location getEndLocation() {
		return endLocation;
	}
	
}