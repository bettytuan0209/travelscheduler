package activities;

import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

public class Activity {
	private Duration duration;
	private ArrayList<Interval> legalTimes;
	private Location location;
	private DateTime scheduledTime;

	public Activity(Duration duration) {
		this.duration = duration;
	}

	public void setLegalTimes() {

	}

	public Duration getDuration() {
		return duration;
	}

	public Location getLocation() {
		return location;
	}

	public DateTime getScheduledTime() {
		return scheduledTime;
	}

}
