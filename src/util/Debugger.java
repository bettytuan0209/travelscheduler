package util;

import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import schedulable.Activity;
import schedulable.LegalTime;
import schedulable.Schedulable;
import schedulable.Transportation;
import time.TimeBlock;
import time.Timeline;

/**
 * A debugger class that contains recyclable debugging code that tests can call
 * to print out data to the console
 * 
 * @author chiao-yutuan
 * 
 */

public class Debugger {
	
	/**
	 * Print out the index of the timeblock and then iterate through a timeline
	 * and print out all the schedulables
	 * 
	 * @param tb
	 *            The Timeblock whose timeline will be printed
	 */
	public static void printSchedulables(TimeBlock tb) {
		System.out.println("==================== TimeBlock " + tb.getIndex()
				+ " ====================");
		printSchedulables(tb.getScheduledActivities());
	}
	
	/**
	 * Iterate through a timeline and print out all the schedulables
	 * 
	 * @param timeline
	 *            The timeline to be printed
	 */
	public static void printSchedulables(Timeline timeline) {
		Set<Map.Entry<DateTime, Schedulable>> entries = timeline.getSchedule()
				.entrySet();
		for (Map.Entry<DateTime, Schedulable> entry : entries) {
			
			// print start time
			System.out.print(entry.getKey().getMillis() + " - "
					+ Util.getEndTime(entry).getMillis() + ": ");
			
			// print schedulable content
			Schedulable value = entry.getValue();
			if (value instanceof Transportation) {
				System.out.println("transportation");
			} else if (value instanceof LegalTime) {
				System.out.println("LegalTime");
			} else if (value instanceof Activity) {
				System.out.println(((Activity) value).title);
			} else {
				throw new ClassCastException(
						"Cannot recognize schedulable class type");
			}
			
		}
	}
}
