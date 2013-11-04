package time;

import java.io.Serializable;
import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import activities.Activity;
import activities.Location;

public class TimeBlock implements Serializable {
	private static final long serialVersionUID = -2954314114527434246L;
	private int index;
	private Interval timeSpan;
	private Location startLocation;
	private Location endLocation;
	private ArrayList<Activity> scheduledActivities; // map from begin time to
														// activity

	public TimeBlock() {
	}

	public Location getStartLocation() {
		return startLocation;
	}

	public Location getEndLocation() {
		return endLocation;
	}

	public boolean scheduleActivity(DateTime startTime, Activity activity) {
		// if timeframe available on this timeblock, insert
		int index;
		if ((index = timeSegmentFree(startTime, activity.getDuration())) >= 0) {
			scheduledActivities.add(index, activity);
			return true;
		}
		return false;
	}

	public boolean scheduleEarliestFree(Activity activity) {
		return scheduleActivity(earliestFree(), activity);
	}

	private int timeSegmentFree(DateTime startTime, Duration duration) {
		// try to find the index to insert this activity

		// if can't find one, return -1
		return -1;
	}

	public DateTime earliestFree() {
		return new DateTime();
	}

}