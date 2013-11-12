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
	
}
