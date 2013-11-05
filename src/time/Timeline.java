package time;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.Interval;

public class Timeline {
	private static final long serialVersionUID = -7686653954597194859L;
	private Interval interval;
	private TreeMap<DateTime, Schedulable> schedule;
	
	public Timeline(Interval interval) {
		this.interval = interval;
		schedule = new TreeMap<DateTime, Schedulable>();
	}
	
	public boolean scheduleAfter(DateTime startTime, Schedulable schedulable) {
		
		// if the earliest possible time
		// if it's after startTime
		// find transportation time
		// if the duration of transportation and activity fits
		// insert and return true
		
		return false;
	}
	
	public boolean schedule(DateTime startTime, Schedulable schedulable) {
		// check if within the interval of this timeline
		
		if (isInclusiveAfter(startTime, interval.getStart())
				&& isInclusiveBefore(startTime.plus(schedulable.duration),
						interval.getEnd())) {
			
			Map.Entry<DateTime, Schedulable> lastEntry = schedule.lastEntry();
			
			// check if nothing scheduled or after last scheduled event
			if (lastEntry == null
					|| isInclusiveAfter(startTime, getEndTime(lastEntry))) {
				schedule.put(startTime, schedulable);
				return true;
				
			} else { // somewhere in between already scheduled
				Iterator<Map.Entry<DateTime, Schedulable>> itr = schedule
						.entrySet().iterator();
				while (itr.hasNext()) {
					Map.Entry<DateTime, Schedulable> entry = itr.next();
					
					// occurs before this scheduled event
					if (startTime.isBefore(entry.getKey())) {
						
						// enough time to stick in between
						if (getEndTime(startTime, schedulable).isBefore(
								entry.getKey())) {
							schedule.put(startTime, schedulable);
							return true;
						} else {
							return false;
						}
					} else if (startTime.isBefore(getEndTime(entry))) {
						// occurs during this scheduled event
						return false;
					}
				}
			}
		}
		
		return false;
	}
	
	public DateTime lastEndTime() {
		return getEndTime(schedule.lastEntry());
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
}
