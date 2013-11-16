package time;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import schedulable.LegalTime;
import schedulable.Schedulable;
import util.Util;

public class LegalTimeline extends Timeline {
	private static final long serialVersionUID = 6861305297042866238L;
	
	public LegalTimeline(Interval interval) {
		super(interval);
	}
	
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
	
	public LegalTimeline(Interval interval,
			TreeMap<DateTime, Schedulable> schedule) {
		this(schedule);
		this.interval = new Interval(interval.getStart(), interval.getEnd()
				.plus(1));
	}
	
	@Override
	public boolean schedule(DateTime startTime, Schedulable legalTime) {
		if (!(legalTime instanceof LegalTime)) {
			throw new ClassCastException(
					"Cannot type cast schedulable to LegalTime");
		}
		
		return privateSchedule(startTime, legalTime);
		
	}
	
	public boolean setEarliestAvailable(DateTime start) {
		Iterator<Map.Entry<DateTime, Schedulable>> itr = schedule.entrySet()
				.iterator();
		while (itr.hasNext()) {
			Map.Entry<DateTime, Schedulable> scheduled = itr.next();
			
			if (!(scheduled.getValue() instanceof LegalTime)) {
				throw new ClassCastException(
						"Cannot type cast schedulable to LegalTime");
			}
			
			if (!scheduled.getKey().isBefore(start)) {
				return true;
				
			} else if (!Util.getEndTime(scheduled).isAfter(start)) {
				((LegalTime) scheduled.getValue()).available = false;
				
			} else {
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
	
	public LegalTimeline intersect(LegalTimeline another) {
		LegalTimeline intersection;
		
		// check for boundary
		if (isEmpty()
				|| another.isEmpty()
				|| Util.getEndTime(schedule.lastEntry()).isBefore(
						another.schedule.firstKey())
				|| Util.getEndTime(another.schedule.lastEntry()).isBefore(
						schedule.firstKey())) {
			return new LegalTimeline(new Interval(0, 0));
		}
		
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
}
