package time;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import schedulable.Activity;
import schedulable.Schedulable;
import activities.Location;

/**
 * Represents a block of time that the scheduler will work with. It has a
 * timeline where the activities and transportation will get scheduled on.
 * TimeBlock also contains information about the start and end location of this
 * time blockIt is paired with an AST so that the scheduler can schedule the
 * activities in the AST in the TB.
 * 
 * @author chiao-yutuan
 * 
 */
public class TimeBlock implements Serializable {
	private static final long serialVersionUID = -2954314114527434246L;
	private int index;
	private Location startLocation;
	private Location endLocation;
	private Timeline timeline;
	
	/**
	 * Constructor with all of the fields except the timeline, which will be
	 * created based on the interval given
	 * 
	 * @param index
	 *            The index number of the timeblock. This is what will be used
	 *            to distinguish one TB with another
	 * @param timespan
	 *            The interval of this TB
	 * @param startLocation
	 *            Where the user will be at the start of this TB
	 * @param endLocation
	 *            Where the user will be at the end of this TB
	 */
	public TimeBlock(int index, Interval timespan, Location startLocation,
			Location endLocation) {
		this.index = index;
		this.startLocation = startLocation;
		this.endLocation = endLocation;
		this.timeline = new Timeline(new Interval(timespan));
		
	}
	
	/**
	 * Wrapper around the timeline schedule method
	 * 
	 * @param startTime
	 *            The exact start time of this schedulable
	 * @param schedulable
	 *            Schedulable to schedule
	 * @return True if successfully scheduled. False if otherwise
	 */
	public boolean schedule(DateTime startTime, Schedulable schedulable) {
		return timeline.schedule(startTime, schedulable);
	}
	
	/**
	 * Wrapper around the timeline scheduleAfter. If the Schedulable is an
	 * Activity, it will extract the legal time and pass it in. If it is
	 * something else, this method will assume it can schedule at any time and
	 * creates a timeline that covers the entire duration of this TB
	 * 
	 * @param startTime
	 *            The earliest start time bound
	 * @param schedulable
	 *            Schedulable to schedule
	 * @return True if successfully scheduled. False if otherwise
	 */
	public boolean scheduleAfter(DateTime startTime, Schedulable schedulable) {
		// try to schedule it
		if (schedulable instanceof Activity) {
			Activity activity = (Activity) schedulable;
			return timeline.scheduleAfter(startTime, activity.legalTimeline,
					activity);
		} else {
			return timeline.scheduleAfter(startTime, schedulable);
		}
	}
	
	/**
	 * Schedule after without a stat time bound. This method will pass in time
	 * 0ms as the start time bound
	 * 
	 * @param schedulable
	 *            Schedulable to schedule
	 * @return True if successfully scheduled. False if otherwise
	 */
	public boolean scheduleAfter(Schedulable schedulable) {
		return scheduleAfter(new DateTime(0), schedulable);
	}
	
	/**
	 * Schedule a TB-starting Activity object 1ms before the start time of this
	 * TB. This is necessary because you cannot call schedule to schedule
	 * something outside of the interval of this TB
	 * 
	 * @param activity
	 *            The Activity object to schedule. It must have duration of 1 or
	 *            0
	 * @return True if successfully scheduled. False if otherwise. This method
	 *         could return false if the duration of the Activity is greater
	 *         than 1ms, or there is something scheduled at that time already,
	 *         or the schedule method of Timeline returns false
	 * 
	 */
	public boolean scheduleBeforeTb(Activity activity) {
		// sanitation check for duration overflow
		if (activity.getDuration().getMillis() > 1) {
			return false;
		}
		
		DateTime beforeInterval = timeline.getInterval().getStart().minus(1);
		if (!timeline.hasScheduleStart(beforeInterval)
				&& timeline.schedule.put(beforeInterval, activity) == null) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Schedule a TB-ending Activity object at the end of a TB. If nothing is
	 * scheduled on the end time of the last Schedulable (or start time of the
	 * TB if nothing scheduled), the end activity will be scheduled then. If
	 * there is something scheduled (aka last Schedulable have the duration of
	 * 0ms), it will increase by 1ms and try to schedule there. This means the
	 * latest time the end Activity may be scheduled is the end time of the TB.
	 * This is necessary because you cannot call schedule to schedule something
	 * outside of the interval of this TB (because Timeline is exclusive of its
	 * end time)
	 * 
	 * @param activity
	 *            The Activity object to schedule. It must have duration of 1 or
	 *            0
	 * @return True if successfully scheduled. False if otherwise. This method
	 *         could return false if the duration of the Activity is greater
	 *         than 1ms, or there is something scheduled at that time already,
	 *         or the schedule method of Timeline returns false
	 * 
	 */
	public boolean scheduleAfterTb(Activity activity) {
		// sanitation check for duration overflow
		if (activity.getDuration().getMillis() > 1) {
			return false;
		}
		DateTime last = timeline.lastEndTime();
		if (timeline.hasScheduleStart(last)) {
			last = last.plus(1);
		}
		if (timeline.schedule.put(last, activity) == null) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Print out the index of the timeblock and then iterate through a timeline
	 * and print out all the schedulables
	 * 
	 * @return The textual representation of this timeblock
	 */
	@Override
	public String toString() {
		String result = "";
		result += "==================== TimeBlock " + index
				+ " ====================\n";
		result += timeline.toString();
		return result;
	}
	
	/**
	 * Overrides the object equals() method. Checks all fields to see if
	 * equals(). Consistent with hashCode()
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TimeBlock) {
			TimeBlock other = (TimeBlock) obj;
			if (index == other.index
					&& startLocation.equals(other.startLocation)
					&& endLocation.equals(other.endLocation)
					&& timeline.equals(other.timeline)) {
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
		return new HashCodeBuilder().append(index).append(startLocation)
				.append(endLocation).append(timeline).toHashCode();
	}
	
	/************************* Getters *****************************/
	
	public Interval getInterval() {
		return timeline.interval;
	}
	
	public DateTime lastEndTime() {
		return timeline.lastEndTime();
	}
	
	public Map.Entry<DateTime, Schedulable> getLastScheduled() {
		return timeline.schedule.lastEntry();
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
	
	public Timeline getTimeline() {
		return timeline;
	}
	
}