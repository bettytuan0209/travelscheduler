package time;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

public class Timeline implements Serializable {
	private static final long serialVersionUID = -7686653954597194859L;
	private Interval interval;
	private TreeMap<DateTime, Schedulable> schedule;
	
	public Timeline(Interval interval) {
		// add 1 milliseconds in default to make interval inclusive of end time
		this.interval = new Interval(interval.getStartMillis(),
				interval.getEndMillis() + 1);
		schedule = new TreeMap<DateTime, Schedulable>();
	}
	
	public Timeline(TreeMap<DateTime, Schedulable> schedule) {
		this.schedule = schedule;
		interval = new Interval(schedule.firstKey(),
				getEndTime(schedule.lastEntry()));
	}
	
	public boolean containsSchedulableWithDuration(Duration duration) {
		
		Iterator<Map.Entry<DateTime, Schedulable>> itr = schedule.entrySet()
				.iterator();
		
		while (itr.hasNext()) {
			if (!itr.next().getValue().duration.isShorterThan(duration)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean scheduleAfter(DateTime bound, Timeline legalTimes,
			Schedulable schedulable) {
		DateTime earliestStart = earliestSchedulableLegalAfter(bound,
				legalTimes, schedulable);
		return schedule(earliestStart, schedulable);
	}
	
	public DateTime earliestSchedulableLegalAfter(DateTime bound,
			Timeline legalTimes, Schedulable schedulable) {
		DateTime start;
		
		// check boundary conditions
		if (legalTimes.schedule.isEmpty()) {
			return null;
		}
		
		start = later(bound, legalTimes.schedule.firstKey());
		
		Iterator<Map.Entry<DateTime, Schedulable>> legalItr = legalTimes.schedule
				.entrySet().iterator();
		Iterator<Map.Entry<DateTime, Schedulable>> scheduledItr = schedule
				.entrySet().iterator();
		
		Map.Entry<DateTime, Schedulable> legal = null;
		Map.Entry<DateTime, Schedulable> scheduled = null;
		
		while (true) {
			// out of the interval of this timeline
			if (start.isAfter(interval.getEnd())) {
				return null;
			}
			if (legal == null
					|| getEndTime(legal).isBefore(start)
					|| new Duration(start, getEndTime(legal))
							.isShorterThan(schedulable.duration)) {
				if (legalItr.hasNext()) {
					legal = legalItr.next();
					start = legal.getKey();
					continue;
				} else {
					return null;
				}
			}
			
			if (scheduled == null
					|| (isInclusiveBefore(getEndTime(scheduled), start) && scheduled
							.getKey().isBefore(start))) {
				if (scheduledItr.hasNext()) {
					scheduled = scheduledItr.next();
					continue;
				}
			}
			if (scheduled == null) {
				return start;
			} else if (start.isBefore(scheduled.getKey())) {
				if (!new Duration(start, scheduled.getKey())
						.isShorterThan(schedulable.duration)) {
					return start;
				}
			} else if (start.isEqual(scheduled.getKey())) {
				start = start.plus(1);
			} else {
				start = getEndTime(scheduled);
				continue;
			}
		}
		
	}
	
	public boolean schedule(DateTime startTime, Schedulable schedulable) {
		Interval toSchedule = new Interval(startTime, getEndTime(startTime,
				schedulable));
		
		// check if within the interval of this timeline
		if (interval.contains(toSchedule)) {
			
			Map.Entry<DateTime, Schedulable> lastEntry = schedule.lastEntry();
			
			// check if nothing scheduled or after last scheduled event
			if (lastEntry == null
					|| (isInclusiveAfter(startTime, getEndTime(lastEntry)) && startTime
							.isAfter(lastEntry.getKey()))) {
				schedule.put(startTime, schedulable);
				return true;
				
			} else { // somewhere in between already scheduled
				Iterator<Map.Entry<DateTime, Schedulable>> itr = schedule
						.entrySet().iterator();
				while (itr.hasNext()) {
					Map.Entry<DateTime, Schedulable> entry = itr.next();
					Interval scheduled = new Interval(entry.getKey()
							.getMillis(), getEndTime(entry).getMillis());
					
					if (scheduled.overlaps(toSchedule)) {
						return false;
					}
					// occurs before this scheduled event
					if (toSchedule.getEnd().isBefore(scheduled.getStart())) {
						schedule.put(startTime, schedulable);
						return true;
					}
					
				}
			}
		}
		
		return false;
	}
	
	public Map.Entry<DateTime, Schedulable> unscheduleBefore(DateTime start) {
		Iterator<Map.Entry<DateTime, Schedulable>> itr = schedule.entrySet()
				.iterator();
		while (itr.hasNext()) {
			Map.Entry<DateTime, Schedulable> scheduled = itr.next();
			if (!scheduled.getKey().isBefore(start)) {
				return null;
			} else if (!getEndTime(scheduled).isAfter(start)) {
				schedule.remove(scheduled.getKey());
			} else {
				return scheduled;
			}
		}
		
		return null;
	}
	
	public Schedulable unschedule(DateTime start) {
		return schedule.remove(start);
	}
	
	public boolean hasScheduleStart(DateTime start) {
		return schedule.containsKey(start);
	}
	
	public DateTime lastEndTime() {
		return getEndTime(schedule.lastEntry());
	}
	
	public int getNumActivities() {
		return schedule.size();
	}
	
	public boolean isEmpty() {
		return schedule.size() == 0;
	}
	
	private DateTime getEndTime(Map.Entry<DateTime, Schedulable> entry) {
		return entry.getKey().plus(entry.getValue().duration);
	}
	
	private DateTime getEndTime(DateTime start, Schedulable schedulable) {
		return start.plus(schedulable.duration);
	}
	
	private boolean isInclusiveBefore(DateTime first, DateTime second) {
		return first.isEqual(second) || first.isBefore(second);
	}
	
	private boolean isInclusiveAfter(DateTime first, DateTime second) {
		return first.isEqual(second) || first.isAfter(second);
	}
	
	private DateTime later(DateTime first, DateTime second) {
		if (first.isAfter(second)) {
			return first;
		} else {
			return second;
		}
	}
	
	public Interval getInterval() {
		return interval;
	}
	
	public TreeMap<DateTime, Schedulable> getSchedule() {
		return schedule;
	}
	
}
