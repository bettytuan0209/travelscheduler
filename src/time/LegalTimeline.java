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
		super(schedule);
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
