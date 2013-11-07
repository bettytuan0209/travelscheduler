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
	private String title;
	private LegalTimeline legalTimeline;
	private Location location;
	
	public Activity(Duration duration) {
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
	
	public Activity(String title, Duration duration,
			TreeMap<DateTime, Schedulable> legalTimes) {
		this.title = title;
		this.duration = duration;
		this.legalTimeline = new LegalTimeline(legalTimes);
	}
	
	public Activity(String title, Duration duration, Location location,
			TreeMap<DateTime, Schedulable> legalTimes) {
		this.title = title;
		this.duration = duration;
		this.location = location;
		this.legalTimeline = new LegalTimeline(legalTimes);
		
	}
	
	public boolean forwardChecking() {
		return legalTimeline.enoughLegalTimes(duration);
	}
	
	public boolean addLegalTime(Interval interval) {
		return legalTimeline.schedule(interval.getStart(), new LegalTime(
				interval.toDuration(), true));
	}
	
	public Location getLocation() {
		return location;
	}
	
	public LegalTimeline getLegalTimeline() {
		return legalTimeline;
	}
	
	public String getTitle() {
		return title;
	}
	
}
