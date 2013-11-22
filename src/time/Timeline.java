package time;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import schedulable.LegalTime;
import schedulable.Schedulable;
import util.DeepCopy;
import util.Util;

public class Timeline implements Serializable {
	private static final long serialVersionUID = -7686653954597194859L;
	protected Interval interval;
	protected TreeMap<DateTime, Schedulable> schedule;
	
	protected Timeline() {
	}
	
	public Timeline(Interval interval) {
		// add 1 milliseconds in default to make interval inclusive of end time
		this.interval = new Interval(interval.getStart(), interval.getEnd()
				.plus(1));
		schedule = new TreeMap<DateTime, Schedulable>();
	}
	
	public Timeline(TreeMap<DateTime, Schedulable> schedule) {
		
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
	
	public Timeline(Interval interval, TreeMap<DateTime, Schedulable> schedule) {
		this(schedule);
		this.interval = new Interval(interval.getStart(), interval.getEnd()
				.plus(1));
		;
	}
	
	public boolean scheduleAfter(DateTime bound, Schedulable schedulable) {
		LegalTimeline legalTimeline = new LegalTimeline(new Interval(
				interval.getStart(), interval.getEnd().minus(1)));
		if (legalTimeline.schedule(interval.getStart(), new LegalTime(
				new Duration(legalTimeline.interval.toDuration())))) {
			return scheduleAfter(bound, legalTimeline, schedulable);
		} else {
			return false;
		}
	}
	
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
	
	public DateTime earliestSchedulableLegalAfter(DateTime bound,
			LegalTimeline legalTimes, Schedulable schedulable) {
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
			if (!start.isBefore(interval.getEnd())) {
				return null;
			}
			
			if (legal != null && !(legal.getValue() instanceof LegalTime)) {
				throw new ClassCastException(
						"Cannot type cast schedulable to LegalTime");
			}
			
			// this legalTime block is irrelevent to test for available time
			if (legal == null
					|| !((LegalTime) legal.getValue()).available
					|| Util.getEndTime(legal).isBefore(start)
					|| new Duration(start, Util.getEndTime(legal))
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
				return start;
				
			} else if (start.isEqual(scheduled.getKey())) {
				start = start.plus(1);
			} else {
				start = later(start, Util.getEndTime(scheduled));
				continue;
			}
		}
		
	}
	
	public boolean schedule(DateTime startTime, Schedulable schedulable) {
		return privateSchedule(startTime, schedulable);
	}
	
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
	
	public Schedulable unschedule(DateTime start) {
		return schedule.remove(start);
	}
	
	public boolean hasScheduleStart(DateTime start) {
		return schedule.containsKey(start);
	}
	
	public DateTime lastEndTime() {
		Map.Entry<DateTime, Schedulable> last = schedule.lastEntry();
		if (last == null) {
			return interval.getStart();
		} else {
			return Util.getEndTime(last);
		}
	}
	
	public int getNumScheduled() {
		return schedule.size();
	}
	
	public boolean isEmpty() {
		return schedule.size() == 0;
	}
	
	protected DateTime later(DateTime first, DateTime second) {
		if (first.isAfter(second)) {
			return first;
		} else {
			return second;
		}
	}
	
	protected DateTime earlier(DateTime first, DateTime second) {
		if (first.isBefore(second)) {
			return first;
		} else {
			return second;
		}
	}
	
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
	
	public Interval getInterval() {
		return interval;
	}
	
	@SuppressWarnings("unchecked")
	public TreeMap<DateTime, Schedulable> getSchedule() {
		return (TreeMap<DateTime, Schedulable>) DeepCopy.copy(schedule);
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(). // two randomly chosen prime numbers
										// if deriving:
										// appendSuper(super.hashCode()).
				append(interval).append(schedule).toHashCode();
	}
}
