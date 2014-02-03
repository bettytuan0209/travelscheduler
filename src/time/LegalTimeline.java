package time;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import schedulable.LegalTime;
import schedulable.Schedulable;
import util.Util;

/**
 * This extends the Timeline such that it checks that everything on it is an
 * instance of LegalTime. It also contains some helpful methods that are
 * specific to LegalTimes
 * 
 * @author chiao-yutuan
 * 
 */
public class LegalTimeline extends Timeline {
	private static final long serialVersionUID = 6861305297042866238L;
	
	/**
	 * Wrapper around the parent constructor. The treemap will be initiated but
	 * no schedulable is scheduled. Note that since the interval class is
	 * exclusive of end time, we add 1 millisecond to the end time make it
	 * inclusive of the end time
	 * 
	 * @param interval
	 *            The interval during which this timeline is concerned with.
	 *            Note that timeline automatically adds 1 millisecond at the end
	 */
	public LegalTimeline(Interval interval) {
		super(interval);
	}
	
	/**
	 * 
	 * Constructor that takes a treemap of start time to Schedulable. Timeline
	 * will create the interval based on the scheduled data (aka the interval
	 * will start when the first scheduled event starts and end when the last
	 * event ends plus 1ms). The constructor will iterate through the schedule
	 * and check for invalid entries (overlaps) and insert them one by one so
	 * that the class contains a copy of the passed in object. Will throw an
	 * exception if scheduling fails (because of overlaps).
	 * 
	 * @throws IllegalArgumentException
	 *             Throws exception if there is an overlap in the treemap
	 * 
	 * @param schedule
	 *            The schedule represented by a treemap
	 * 
	 */
	public LegalTimeline(TreeMap<DateTime, Schedulable> schedule) {
		
		if (schedule.isEmpty()) {
			interval = new Interval(0, 0);
			this.schedule = new TreeMap<DateTime, Schedulable>();
		} else {
			// add 1 milliseconds in default to be inclusive of end time
			interval = new Interval(schedule.firstKey(), Util.getEndTime(
					schedule.lastEntry()).plus(1));
			this.schedule = new TreeMap<DateTime, Schedulable>();
			
			// insert the schedule to ensure it is legal
			Iterator<Map.Entry<DateTime, Schedulable>> itr = schedule
					.entrySet().iterator();
			
			while (itr.hasNext()) {
				Map.Entry<DateTime, Schedulable> toInsert = itr.next();
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
	 * 
	 */
	public LegalTimeline(Interval interval,
			TreeMap<DateTime, Schedulable> schedule) {
		this(schedule);
		this.interval = new Interval(interval.getStart(), interval.getEnd()
				.plus(1));
	}
	
	/**
	 * Scheule a free LegalTime
	 * 
	 * @param start
	 *            The exact start time of this LegalTime segment
	 * @param end
	 *            The exact end time of this LegalTime segment
	 * 
	 * @return True if scheduled successfully. False if otherwise
	 */
	public boolean schedule(long start, long end) {
		return privateSchedule(new DateTime(start), new LegalTime(new Duration(
				end - start)));
	}
	
	/**
	 * Scheule a Schedulable at a specific start time. Will throw an exception
	 * if the Schedulable is not an instance of LegalTime
	 * 
	 * @param startTime
	 *            The exact time to schedule it
	 * @param schedulable
	 *            The LegalTime to schedule
	 * @throws ClassCastException
	 *             Throws exception if the Schedulable passed in is not an
	 *             instance of LegalTime
	 * @return True if scheduled successfully. False if otherwise
	 */
	@Override
	public boolean schedule(DateTime startTime, Schedulable legalTime) {
		if (!(legalTime instanceof LegalTime)) {
			throw new ClassCastException(
					"Cannot type cast schedulable to LegalTime");
		}
		
		return privateSchedule(startTime, legalTime);
		
	}
	
	/**
	 * Automatically goes through the LegalTimeline and set everything before
	 * the given time as not available. If the bound occurs in the middle of a
	 * LegalTime. This method will break it and re-schedule the first half as
	 * not available and the second half as available
	 * 
	 * @param start
	 *            The exact earliest available time. Anything before that is
	 *            unavailable. Anything at or after this time is not changed
	 * @throws ClassCastException
	 *             Throws exception if some Schedulable on the LegalTimeline is
	 *             not a LegalTime
	 * @return True if procedure succeeded. False if something went wrong when
	 *         trying to break 1 segment and re-schedule the 2 halves
	 */
	public boolean setEarliestAvailable(DateTime start) {
		Iterator<Map.Entry<DateTime, Schedulable>> itr = schedule.entrySet()
				.iterator();
		while (itr.hasNext()) {
			Map.Entry<DateTime, Schedulable> scheduled = itr.next();
			
			// some Schedulable is not an instance of LegalTime
			if (!(scheduled.getValue() instanceof LegalTime)) {
				throw new ClassCastException(
						"Cannot type cast schedulable to LegalTime");
			}
			
			// The next Schedulable is at or after the bound, we are done.
			if (!scheduled.getKey().isBefore(start)) {
				return true;
				
			} else if (!Util.getEndTime(scheduled).isAfter(start)) {
				// entire Schedulable is before the bound, set availability to
				// false
				((LegalTime) scheduled.getValue()).available = false;
				
			} else {
				// the bound occurs in the middle of this Schedulable
				Duration available = scheduled.getValue().getDuration()
						.minus(new Duration(scheduled.getKey(), start));
				Duration unavailable = scheduled.getValue().getDuration()
						.minus(available);
				
				if (schedule.remove(scheduled.getKey()) != null
						&& schedule(start, new LegalTime(available, true))
						&& schedule(scheduled.getKey(), new LegalTime(
								unavailable, false))) {
					return true;
				} else {
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Checks if there is enough legal time available to schedule something.
	 * This is usually used to do forward checking
	 * 
	 * @param duration
	 *            The duration of whatever you are checking availability for
	 * @throws ClassCastExcpetion
	 *             throws exception if some Schedulable is not an instance of
	 *             LegalTime
	 * @return True if there exists some LegalTime segment that is available and
	 *         is long enough to cover the duration given
	 */
	public boolean enoughLegalTimes(Duration duration) {
		Iterator<Map.Entry<DateTime, Schedulable>> itr = schedule.entrySet()
				.iterator();
		while (itr.hasNext()) {
			Schedulable legal = itr.next().getValue();
			
			if (!(legal instanceof LegalTime)) {
				throw new ClassCastException(
						"Cannot type cast schedulable to LegalTime");
			}
			
			if (((LegalTime) legal).available
					&& !legal.getDuration().isShorterThan(duration)) {
				return true;
			}
			
		}
		return false;
	}
	
	/**
	 * Find the intersection of 2 legalTimeline's
	 * 
	 * @param another
	 *            Another LegalTimeline to intersect with
	 * @return the LegalTimeline that consists of the intersection of the two
	 *         LegalTimeline's
	 */
	
	public LegalTimeline intersect(LegalTimeline another) {
		LegalTimeline intersection;
		
		// check for boundary cases
		if (isEmpty()
				|| another.isEmpty()
				|| Util.getEndTime(schedule.lastEntry()).isBefore(
						another.schedule.firstKey())
				|| Util.getEndTime(another.schedule.lastEntry()).isBefore(
						schedule.firstKey())) {
			return new LegalTimeline(new Interval(0, 0));
		}
		
		// Creates a LegalTimeline that has the interval from the latest start
		// time to the earliest end time of the two timelines
		DateTime latestStart = this.later(schedule.firstKey(),
				another.schedule.firstKey());
		DateTime earliestEnd = earlier(Util.getEndTime(schedule.lastEntry()),
				Util.getEndTime(another.schedule.lastEntry()));
		intersection = new LegalTimeline(new Interval(latestStart, earliestEnd));
		
		Iterator<Map.Entry<DateTime, Schedulable>> mineItr = schedule
				.entrySet().iterator();
		Iterator<Map.Entry<DateTime, Schedulable>> theirItr = another.schedule
				.entrySet().iterator();
		
		Map.Entry<DateTime, Schedulable> mine = null;
		Map.Entry<DateTime, Schedulable> their = null;
		
		while (true) {
			
			// this block of mine is irrelevant now
			if (mine == null
					|| (their != null && Util.getEndTime(mine).isBefore(
							their.getKey()))) {
				if (mineItr.hasNext()) {
					mine = mineItr.next();
					continue;
				} else {
					return intersection;
				}
			}
			
			// this block of their is irrelevant now
			if (their == null || Util.getEndTime(their).isBefore(mine.getKey())) {
				if (theirItr.hasNext()) {
					their = theirItr.next();
					continue;
				} else {
					return intersection;
				}
			}
			
			// schedule intersection
			latestStart = later(mine.getKey(), their.getKey());
			earliestEnd = earlier(Util.getEndTime(mine), Util.getEndTime(their));
			if (!intersection.schedule(latestStart, new LegalTime(new Duration(
					latestStart, earliestEnd)))) {
				throw new UnsupportedOperationException(
						"Cannot insert intersection.");
			}
			
			// forward one of the schedulables
			if (earliestEnd.equals(Util.getEndTime(mine))) {
				if (mineItr.hasNext()) {
					mine = mineItr.next();
					continue;
				} else {
					return intersection;
				}
			} else {
				if (theirItr.hasNext()) {
					their = theirItr.next();
					continue;
				} else {
					return intersection;
				}
			}
			
		}
	}
	
	@Override
	public String toString() {
		String result = "";
		Set<Map.Entry<DateTime, Schedulable>> entries = getSchedule()
				.entrySet();
		for (Map.Entry<DateTime, Schedulable> entry : entries) {
			
			// print start time
			if (!result.equals("")) {
				result += ", ";
			}
			
			result += entry.getKey().getMillis() + " - "
					+ Util.getEndTime(entry).getMillis();
			
		}
		return result;
	}
	
}
