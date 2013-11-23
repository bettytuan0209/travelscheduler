package util;

import java.util.Map;

import org.joda.time.DateTime;

import schedulable.Schedulable;

public class Util {
	
	public static DateTime getEndTime(Map.Entry<DateTime, Schedulable> entry) {
		return entry.getKey().plus(entry.getValue().getDuration());
	}
	
	public static DateTime getEndTime(DateTime start, Schedulable schedulable) {
		return start.plus(schedulable.getDuration());
	}
	
}
