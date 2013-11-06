package activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.MutableInterval;

import time.Schedulable;
import time.Timeline;

public class Activity extends Schedulable implements Serializable {
	private static final long serialVersionUID = -17837538834918894L;
	// protected Timeline legalTimes;
	private Timeline timeline;
	protected TreeMap<DateTime, MutableInterval> legalTimes;
	private Location location;
	
	public Activity(Duration duration) {
		this.duration = duration;
		legalTimes = new TreeMap<DateTime, MutableInterval>();
	}
	
	public Activity(Duration duration, Location location) {
		this.duration = duration;
		this.location = location;
		legalTimes = new TreeMap<DateTime, MutableInterval>();
		
	}
	
	public void addLegalTime(MutableInterval newInterval) {
		// merge any legalTime
		ArrayList<MutableInterval> values = new ArrayList<MutableInterval>(
				legalTimes.values());
		for (int i = 0; i < values.size(); i++) {
			MutableInterval legalTime = values.get(i);
			if (shouldMerge(legalTime, newInterval)) {
				legalTime = mergeIntervals(legalTime, newInterval);
				legalTimes.put(legalTime.getStart(), legalTime);
				if (i < values.size() - 1
						&& shouldMerge(legalTime, values.get(i + 1))) {
					legalTime = mergeIntervals(legalTime, values.get(i + 1));
					legalTimes.put(legalTime.getStart(), legalTime);
					legalTimes.remove(values.get(i + 1).getStart());
					
				}
				return;
			}
		}
		// otherwise, add interval as-is
		legalTimes.put(newInterval.getStart(), newInterval);
	}
	
	public void setEarlistStartTime(DateTime start) {
		// update legal times
		for (MutableInterval legalTime : legalTimes.values()) {
			if (legalTime.isBefore(start)) {
				legalTimes.remove(legalTime.getStart());
				return;
			} else if (legalTime.contains(start)) {
				legalTimes.remove(legalTime.getStart());
				legalTime.setStart(start);
				legalTimes.put(legalTime.getStart(), legalTime);
				return;
			}
		}
	}
	
	public boolean enoughLegalTimes() {
		// if there is any legalTimes to schedule this event
		for (MutableInterval legalTime : legalTimes.values()) {
			if (legalTime.toDuration().isEqual(duration)
					|| legalTime.toDuration().isLongerThan(duration)) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean shouldMerge(MutableInterval a, MutableInterval b) {
		return a.getEnd().equals(b.getStart())
				|| a.getStart().equals(b.getEnd()) || a.overlaps(b);
	}
	
	protected MutableInterval mergeIntervals(MutableInterval intervalA,
			MutableInterval intervalB) {
		
		return new MutableInterval(Math.min(intervalA.getStartMillis(),
				intervalB.getStartMillis()), Math.max(intervalA.getEndMillis(),
				intervalB.getEndMillis()));
	}
	
	public Duration getDuration() {
		return duration;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public Timeline getLegalTimes() {
		return timeline;
	}
	
}
