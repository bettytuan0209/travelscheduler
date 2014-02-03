package time;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import schedulable.Activity;
import schedulable.LegalTime;
import schedulable.Schedulable;
import schedulable.Transportation;
import util.DeepCopy;
import util.Util;

/**
 * Timeline class that serves as a container of scheduled activities. It has an
 * interval and a TreeMap that maps a DateTime object representing the start
 * time of a schedulable, to a Schedulable object. For example: 10 => Activity
 * "museum" means that the activity "museum" is scheduled to start at 10
 * 
 * @author chiao-yutuan
 * 
 */
public class Timeline implements Serializable {
	private static final long serialVersionUID = -7686653954597194859L;
	protected Interval interval;
	protected TreeMap<DateTime, Schedulable> schedule;
	
	/**
	 * For testing
	 */
	protected Timeline() {
	}
	
	/**
	 * Constructor that only has an interval. The treemap will be initiated but
	 * no schedulable is scheduled. Note that since the interval class is
	 * exclusive of end time, we add 1 millisecond to the end time make it
	 * inclusive of the end time
	 * 
	 * @param interval
	 *            The interval during which this timeline is concerned with.
	 *            Note that timeline automatically adds 1 millisecond at the end
	 */
	public Timeline(Interval interval) {
		// add 1 milliseconds in default to make interval inclusive of end time
		this.interval = new Interval(interval.getStart(), interval.getEnd()
				.plus(1));
		schedule = new TreeMap<DateTime, Schedulable>();
	}
	
	/**
	 * Constructor that takes a treemap of start time to Schedulable. Timeline
	 * will create the interval based on the scheduled data (aka the interval
	 * will start when the first scheduled event starts and end when the last
	 * event ends plus 1ms). The constructor will iterate through the schedule
	 * and try to insert them one by one so that the class contains a copy of
	 * the passed in object. Will throw an exception if scheduling fails
	 * (because of overlaps).
	 * 
	 * @throws IllegalArgumentException
	 *             Throws exception if there is an overlap in the treemap
	 * @param schedule
	 *            The schedule represented by a treemap
	 */
	public Timeline(TreeMap<DateTime, Schedulable> schedule) {
		
		if (schedule.isEmpty()) {
			interval = new Interval(0, 0);
			this.schedule = new TreeMap<DateTime, Schedulable>();
		} else {
			// add 1 milliseconds in default to be inclusive of end time
			interval = new Interval(schedule.firstKey(), Util.getEndTime(
					schedule.lastEntry()).plus(1));
			this.schedule = new TreeMap<DateTime, Schedulable>();
			
			// iterate through the schedule to ensure it is legal
			Iterator<Map.Entry<DateTime, Schedulable>> itr = schedule
					.entrySet().iterator();
			
			while (itr.hasNext()) {
				Map.Entry<DateTime, Schedulable> toInsert = itr.next();
				
				// if overlap found
				if (!schedule(toInsert.getKey(), toInsert.getValue())) {
					throw new IllegalArgumentException(
							"TreeMap is invalid. Check for overlapping schedulables");
				}
			}
		}
		
	}
	
	/**
	 * A constructor that contains both an interval and a treemap. See public
	 * Timeline(TreeMap<DateTime, Schedulable> schedule) for constructing with
	 * an existing schedule. The Timeline will contain a new Interval with 1
	 * millisecond added at the end
	 * 
	 * @param interval
	 *            The interval of the timeline
	 * @param schedule
	 *            The schedule represented as a treemap
	 */
	public Timeline(Interval interval, TreeMap<DateTime, Schedulable> schedule) {
		this(schedule);
		this.interval = new Interval(interval.getStart(), interval.getEnd()
				.plus(1));
		
	}
	
	/**
	 * Schedule a schedulable after a given earliest start time bound. Assume
	 * the schedulable can be scheduled any time on this timeline
	 * 
	 * @param bound
	 *            Earliest start time to schedule
	 * @param schedulable
	 *            The schedulable to schedule
	 * @return True if successfully scheduled. False if cannot schedule on the
	 *         timeline
	 */
	public boolean scheduleAfter(DateTime bound, Schedulable schedulable) {
		// create a legalTimeline that covers the interval of this timeline
		LegalTimeline legalTimeline = new LegalTimeline(new Interval(
				interval.getStart(), interval.getEnd().minus(1)));
		
		// schedule a legal time so that the schedulable is legal to schedule
		// any time on this timeline
		if (legalTimeline.schedule(interval.getStart(), new LegalTime(
				new Duration(legalTimeline.interval.toDuration())))) {
			return scheduleAfter(bound, legalTimeline, schedulable);
		} else {
			return false;
		}
	}
	
	/**
	 * Schedule a schedulable after a given earliest start time bound and given
	 * the legal time to schedule this activity
	 * 
	 * @param bound
	 *            Earliest start time to schedule
	 * @param legalTimes
	 *            The LegalTimeline representing the times that it can be
	 *            scheduled
	 * @param schedulable
	 *            Schedulable to schedule
	 * @return True if successfully scheduled. False if cannot schedule on the
	 *         timeline
	 */
	public boolean scheduleAfter(DateTime bound, LegalTimeline legalTimes,
			Schedulable schedulable) {
		DateTime earliestStart = earliestSchedulableLegalAfter(bound,
				legalTimes, schedulable);
		if (earliestStart == null) {
			return false;
		} else {
			return schedule(earliestStart, schedulable);
		}
	}
	
	/**
	 * Find the earliest time that we can schedule a Schedulable on this
	 * timeline without conflicts, with consideration of the eraliest start time
	 * and the legal times for this Schedulable
	 * 
	 * @param bound
	 *            Earliest start time
	 * @param legalTimes
	 *            The LegalTimeline representing the times that it can be
	 *            scheduled
	 * @param schedulable
	 *            Schedulable to schedule
	 * @return Earliest time that we can schedule this Schedulable, represented
	 *         in DateTime
	 */
	public DateTime earliestSchedulableLegalAfter(DateTime bound,
			LegalTimeline legalTimes, Schedulable schedulable) {
		DateTime start;
		
		// check boundary conditions
		if (legalTimes.schedule.isEmpty()) {
			return null;
		}
		
		// forward to beginning of the first legal time if appropriate
		start = later(bound, legalTimes.schedule.firstKey());
		
		Iterator<Map.Entry<DateTime, Schedulable>> legalItr = legalTimes.schedule
				.entrySet().iterator();
		Iterator<Map.Entry<DateTime, Schedulable>> scheduledItr = schedule
				.entrySet().iterator();
		
		Map.Entry<DateTime, Schedulable> legal = null;
		Map.Entry<DateTime, Schedulable> scheduled = null;
		
		// work through the legal timeline and current scheduled timeline until
		// found some result
		while (true) {
			// out of the interval of this timeline
			if (!start.isBefore(interval.getEnd())) {
				return null;
			}
			
			// something on the legal timeline isn't a LegalTime
			if (legal != null && !(legal.getValue() instanceof LegalTime)) {
				throw new ClassCastException(
						"Cannot type cast schedulable to LegalTime");
			}
			
			// this legalTime block is irrelevent to test for available time
			if (legal == null || !((LegalTime) legal.getValue()).available
					|| Util.getEndTime(legal).isBefore(start) // whole segment
																// is before our
																// pointer
					|| new Duration(start, Util.getEndTime(legal)) // not long
																	// enough
																	// available
																	// time to
																	// schedule
							.isShorterThan(schedulable.getDuration())) {
				if (legalItr.hasNext()) {
					legal = legalItr.next();
					start = later(start, legal.getKey());
					continue;
				} else {
					return null;
				}
			}
			
			// this schedulable is irrelevant to test for available time
			if (scheduled == null
			// segment ends before or at the same time as our pointer but the
			// schedulable is not 0 seconds
					|| (!Util.getEndTime(scheduled).isAfter(start) && scheduled
							.getKey().isBefore(start))) {
				if (scheduledItr.hasNext()) {
					scheduled = scheduledItr.next();
					continue;
				} else {
					scheduled = null;
				}
			}
			
			// out of schedulables
			if (scheduled == null) {
				return start;
				
			} else if (start.isBefore(scheduled.getKey())
					&& !new Duration(start, scheduled.getKey())
							.isShorterThan(schedulable.getDuration())) {
				// enough time before the next schedulable to shedule this
				// activity
				return start;
				
			} else if (start.isEqual(scheduled.getKey())) {
				// if pointing at the start of a scheulable, add 1 ms and try
				// again in case the schedulable is 0ms
				start = start.plus(1);
			} else {
				// forward the pointer to the end this schedulable, if
				// appropriate
				start = later(start, Util.getEndTime(scheduled));
				continue;
			}
		}
		
	}
	
	/**
	 * Scheule a Schedulable at a specific start time
	 * 
	 * @param startTime
	 *            The exact time to schedule it
	 * @param schedulable
	 *            Schedulable to Schedule
	 * @return True if scheduled successfully. False if otherwise
	 */
	public boolean schedule(DateTime startTime, Schedulable schedulable) {
		return privateSchedule(startTime, schedulable);
	}
	
	/**
	 * Helper schedule method
	 * 
	 * @param startTime
	 *            the exact time to schedule
	 * @param schedulable
	 *            Schedulable to schedule
	 * @return True if scheduled successfully. False if otherwise
	 */
	protected boolean privateSchedule(DateTime startTime,
			Schedulable schedulable) {
		Interval toSchedule = new Interval(startTime, Util.getEndTime(
				startTime, schedulable));
		
		// check if within the interval of this timeline
		if (interval.contains(toSchedule)) {
			
			Map.Entry<DateTime, Schedulable> lastEntry = schedule.lastEntry();
			
			// check if nothing scheduled or after last scheduled event
			if (lastEntry == null
					|| (!startTime.isBefore(Util.getEndTime(lastEntry)) && startTime
							.isAfter(lastEntry.getKey()))) {
				schedule.put(startTime, schedulable);
				return true;
				
			} else { // somewhere in between already scheduled
				Iterator<Map.Entry<DateTime, Schedulable>> itr = schedule
						.entrySet().iterator();
				while (itr.hasNext()) {
					Map.Entry<DateTime, Schedulable> entry = itr.next();
					Interval scheduled = new Interval(entry.getKey(),
							Util.getEndTime(entry));
					
					// start time or any other part of an existing schedulable
					// overlaps with toSchedule
					if (scheduled.getStart().equals(toSchedule.getStart())
							|| scheduled.overlaps(toSchedule)) {
						return false;
					}
					
					// occurs before this scheduled event
					if (!toSchedule.getEnd().isAfter(scheduled.getStart())) {
						schedule.put(startTime, schedulable);
						return true;
					}
					
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Takes some schedulable off the timeline
	 * 
	 * @param start
	 *            the time it is scheduled
	 * @return the previous value being associated with this key or null if
	 *         nothing found
	 */
	public Schedulable unschedule(DateTime start) {
		return schedule.remove(start);
	}
	
	/**
	 * Checks if a Schedulable is scheduled to start at this time
	 * 
	 * @param start
	 *            start time
	 * @return True if a Schedulable is scheduled to start at this time. Flase
	 *         if otherwise
	 */
	public boolean hasScheduleStart(DateTime start) {
		return schedule.containsKey(start);
	}
	
	/**
	 * End time of the last schedulable on the timeline. If nothing scheduled,
	 * return the start time
	 * 
	 * @return End time as DateTime.
	 */
	public DateTime lastEndTime() {
		Map.Entry<DateTime, Schedulable> last = schedule.lastEntry();
		if (last == null) {
			return interval.getStart();
		} else {
			return Util.getEndTime(last);
		}
	}
	
	/**
	 * Get number of scheduled schedulables on the timeline
	 * 
	 * @return number of schedulables on the timeline
	 */
	public int getNumScheduled() {
		return schedule.size();
	}
	
	/**
	 * Checks if the schedule is empty
	 * 
	 * @return True if nothing is scheduled on this timeline. False if otherwise
	 */
	public boolean isEmpty() {
		return schedule.size() == 0;
	}
	
	/**
	 * Compares and return the time that occurs later
	 * 
	 * @param first
	 *            the first time to compare
	 * @param second
	 *            the second time to compare
	 * @return the DateTime that occurs later
	 */
	protected DateTime later(DateTime first, DateTime second) {
		if (first.isAfter(second)) {
			return first;
		} else {
			return second;
		}
	}
	
	/**
	 * Compares and return the time that occurs earlier
	 * 
	 * @param first
	 *            the first time to compare
	 * @param second
	 *            the second time to compare
	 * @return the DateTime that occurs earlier
	 */
	protected DateTime earlier(DateTime first, DateTime second) {
		if (first.isBefore(second)) {
			return first;
		} else {
			return second;
		}
	}
	
	public Interval getInterval() {
		return interval;
	}
	
	/**
	 * Getter of the treemap. Returns a deep copy of the treemap to prevent
	 * people from modifying it and schedule something illegally
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TreeMap<DateTime, Schedulable> getSchedule() {
		return (TreeMap<DateTime, Schedulable>) DeepCopy.copy(schedule);
	}
	
	/**
	 * Iterate through a timeline and print out all the schedulables
	 * 
	 * @return The textual representation of this timeline
	 */
	@Override
	public String toString() {
		String result = "";
		Set<Map.Entry<DateTime, Schedulable>> entries = getSchedule()
				.entrySet();
		for (Map.Entry<DateTime, Schedulable> entry : entries) {
			
			// print start time
			result += entry.getKey().getMillis() + " - "
					+ Util.getEndTime(entry).getMillis() + ": ";
			
			// print schedulable content
			Schedulable value = entry.getValue();
			if (value instanceof Transportation) {
				result += "transportation\n";
			} else if (value instanceof LegalTime) {
				result += "LegalTime\n";
			} else if (value instanceof Activity) {
				result += ((Activity) value).title + "\n";
			} else {
				throw new ClassCastException(
						"Cannot recognize schedulable class type");
			}
			
		}
		return result;
	}
	
	/**
	 * Overrides the object equals() method. Checks all fields to see if
	 * equals(). Consistent with hashCode()
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Timeline) {
			Timeline other = (Timeline) obj;
			if (interval.equals(other.interval)
					&& schedule.equals(other.schedule)) {
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
		return new HashCodeBuilder().append(interval).append(schedule)
				.toHashCode();
	}
}
