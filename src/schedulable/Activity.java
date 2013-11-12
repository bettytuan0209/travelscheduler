package schedulable;

import java.io.Serializable;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import time.LegalTimeline;
import activities.Location;

public class Activity extends Schedulable implements Serializable {
	private static final long serialVersionUID = -17837538834918894L;
	public String title;
	public Location location;
	private LegalTimeline legalTimeline;
	
	public Activity(Duration duration) {
		this.title = "";
		this.duration = duration;
		this.legalTimeline = new LegalTimeline(
				new TreeMap<DateTime, Schedulable>());
	}
	
	public Activity(String title, Duration duration, Location location) {
		this.title = title;
		this.duration = duration;
		this.location = location;
		this.legalTimeline = new LegalTimeline(
				new TreeMap<DateTime, Schedulable>());
		
	}
	
	public Activity(String title, Duration duration, LegalTimeline legalTimeline) {
		this.title = title;
		this.duration = duration;
		this.legalTimeline = legalTimeline;
	}
	
	public Activity(String title, Duration duration, Location location,
			LegalTimeline legalTimeline) {
		this.title = title;
		this.duration = duration;
		this.location = location;
		this.legalTimeline = legalTimeline;
		
	}
	
	public boolean forwardChecking() {
		return legalTimeline.enoughLegalTimes(duration);
	}
	
	public boolean addLegalTime(Interval interval) {
		return legalTimeline.schedule(interval.getStart(), new LegalTime(
				interval.toDuration(), true));
	}
	
	public LegalTimeline getLegalTimeline() {
		return legalTimeline;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Activity) {
			Activity other = (Activity) obj;
			if (duration.equals(other.duration)
					&& (title == null || title.equals(other.title))
					&& (location == null || location.equals(other.location))
					&& legalTimeline.equals(other.legalTimeline)) {
				return true;
			}
			
		}
		return false;
	}
	
}
