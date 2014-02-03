package schedulable;

import java.io.Serializable;
import java.util.TreeMap;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import time.LegalTimeline;
import activities.Location;

/**
 * Activity is a schedulable that represents any event that wants to be
 * scheduled.
 * 
 * @author chiao-yutuan
 * 
 */

public class Activity extends Schedulable implements Serializable {
	private static final long serialVersionUID = -17837538834918894L;
	public String title;
	public Location location;
	public LegalTimeline legalTimeline; // A timeline of legal open hours
	
	/**
	 * Constructor with only duration. Title will be an empty string "".
	 * LegalTimeline will be empty without an interval
	 * 
	 * @param duration
	 *            The duration of the activity
	 */
	public Activity(Duration duration) {
		this.title = "";
		this.duration = duration;
		this.legalTimeline = new LegalTimeline(
				new TreeMap<DateTime, Schedulable>());
	}
	
	/**
	 * Basic constructor without legal times.
	 * 
	 * @param title
	 *            Title of the activity
	 * @param duration
	 *            Duration of the activity
	 * @param location
	 *            Location of the activity. To be used when handling
	 *            transportations
	 */
	public Activity(String title, Duration duration, Location location) {
		this.title = title;
		this.duration = duration;
		this.location = location;
		this.legalTimeline = new LegalTimeline(
				new TreeMap<DateTime, Schedulable>());
		
	}
	
	/**
	 * Constructor without location
	 * 
	 * @param title
	 *            Title of the activity
	 * @param duration
	 *            Duration of the activity
	 * @param location
	 *            Location of the activity. To be used when handling
	 *            transportations
	 * @param legalTimeline
	 *            LegalTimeline of this activity. Should have the legal times of
	 *            the activity scheduled. Activity will directly point to the
	 *            legal timeline instead of making a copy
	 */
	public Activity(String title, Duration duration, LegalTimeline legalTimeline) {
		this.title = title;
		this.duration = duration;
		this.legalTimeline = legalTimeline;
	}
	
	/**
	 * Constructor with all fields of the class
	 * 
	 * @param title
	 *            Title of the activity
	 * @param duration
	 *            Duration of the activity
	 * @param legalTimeline
	 *            LegalTimeline of this activity. Should have the legal times of
	 *            the activity scheduled. Activity will directly point to the
	 *            legal timeline instead of making a copy
	 */
	public Activity(String title, Duration duration, Location location,
			LegalTimeline legalTimeline) {
		this.title = title;
		this.duration = duration;
		this.location = location;
		this.legalTimeline = legalTimeline;
		
	}
	
	/**
	 * Checks if the legal timeline of this activity contains some segment that
	 * is enough to schedule this activity itself
	 * 
	 * @return True if found enough legal available time to schedule this
	 *         activity. False otherwise
	 * 
	 */
	public boolean forwardChecking() {
		return legalTimeline.enoughLegalTimes(duration);
	}
	
	/**
	 * Add a block of legal time to the legal timeline
	 * 
	 * @param interval
	 *            the interval of legal time
	 * @return True if scheduled successfully. False if otherwise
	 */
	public boolean addLegalTime(Interval interval) {
		return legalTimeline.schedule(interval.getStart(), new LegalTime(
				interval.toDuration(), true));
	}
	
	/**
	 * Overrides the object equals() method. Checks all fields to see if
	 * equals(). Consistent with hashCode()
	 */
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
	
	/**
	 * Overrides the object hashCode() method. Creates a hash using all fields
	 * in the class. Consistent with equals()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(duration).append(title)
				.append(location).append(legalTimeline).toHashCode();
		
	}
	
	@Override
	public String toString() {
		String result = "Activity ";
		result += title + "; ";
		result += "duration " + duration.getMillis() + "; ";
		result += "location " + location + "; ";
		result += "legal times " + legalTimeline.toString() + ".";
		return result;
	}
	
}
