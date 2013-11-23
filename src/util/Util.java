package util;

import java.util.Map;

import org.joda.time.DateTime;

import schedulable.Schedulable;

/**
 * Util class that contains some useful recyclable codes
 * 
 * @author chiao-yutuan
 * 
 */
public class Util {
	
	/**
	 * Calculates the end time of a scheduled activity
	 * 
	 * @param entry
	 *            The HashMap entry of the start time mapped to the schedulable
	 * @return the end time of the activity
	 */
	public static DateTime getEndTime(Map.Entry<DateTime, Schedulable> entry) {
		return entry.getKey().plus(entry.getValue().getDuration());
	}
	
	/**
	 * Calculates the end time of a scheduled activity
	 * 
	 * @param start
	 *            The start time of the schedulable
	 * @param schedulable
	 *            The schedulable itself
	 * @return the end time of the activity if it is scheduled at the start time
	 */
	
	public static DateTime getEndTime(DateTime start, Schedulable schedulable) {
		return start.plus(schedulable.getDuration());
	}
	
}
