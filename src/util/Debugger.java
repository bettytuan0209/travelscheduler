package util;

import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import schedulable.Activity;
import schedulable.Schedulable;
import schedulable.Transportation;
import time.Timeline;

public class Debugger {
	public static void printSchedulables(Timeline timeline) {
		Set<Map.Entry<DateTime, Schedulable>> entries = timeline.getSchedule()
				.entrySet();
		for (Map.Entry<DateTime, Schedulable> entry : entries) {
			System.out.print(entry.getKey().getMillis() + " - "
					+ Util.getEndTime(entry).getMillis() + ": ");
			Schedulable value = entry.getValue();
			if (value instanceof Transportation) {
				System.out.println("transportation");
			} else {
				System.out.println(((Activity) value).title);
			}
			
		}
	}
}
